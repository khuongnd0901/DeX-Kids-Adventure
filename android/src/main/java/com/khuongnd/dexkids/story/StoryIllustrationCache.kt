package com.khuongnd.dexkids.story

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Looper
import android.util.AtomicFile
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/** Optional authoring infrastructure only. No provider is installed and playback never contacts one.
 * Returned files still require worker decoding and GL-thread upload by a future renderer adapter. */
class StoryIllustrationCache @JvmOverloads constructor(context: Context, private val adapter: Adapter = UnsupportedAdapter) {
    data class Capability(val supported: Boolean, val priceDisclosed: Boolean)
    data class Consent(val parentOptIn: Boolean, val explicitCostConfirmed: Boolean, val beforePublication: Boolean)
    interface Adapter {
        /** Local metadata only; this method must never dispatch HTTP. */
        fun capability(): Capability
        /** General character-free scenery only. Never pass child references, speech or location. */
        fun authorBackground(generalScene: String): ByteArray
    }
    object UnsupportedAdapter : Adapter {
        override fun capability() = Capability(false, false)
        override fun authorBackground(generalScene: String): ByteArray = error("No image provider installed")
    }
    private val root = File(context.applicationContext.filesDir, "story-illustrations-v1")
    private val pngSignature = byteArrayOf(-119,80,78,71,13,10,26,10)
    /** Explicit opt-in is checked before even querying the adapter. Staged art is never replayable. */
    fun authorAndStage(key: String, generalScene: String, consent: Consent): Boolean = synchronized(lock) {
        worker(); validKey(key)
        if (!consent.parentOptIn || !consent.explicitCostConfirmed || !consent.beforePublication) return@synchronized false
        val capability = runCatching {adapter.capability()}.getOrElse {return@synchronized false}
        if (!capability.supported || !capability.priceDisclosed) return@synchronized false
        // Finite generic backdrop vocabulary prevents accidental personal text or child references.
        require(generalScene in setOf("GARDEN", "FOREST", "SKY", "WATER", "MOUNTAIN"))
        if(Thread.currentThread().isInterrupted)return@synchronized false
        runCatching {
            val bytes=adapter.authorBackground(generalScene)
            if(Thread.currentThread().isInterrupted)return@synchronized false
            stageLocked(key,bytes)
            true
        }.getOrDefault(false)
    }
    /** Stage a provider result before adult visual review. No approval is inferred from authoring. */
    fun stageForParentReview(key: String, png: ByteArray) = synchronized(lock) {
        worker(); validKey(key); stageLocked(key, png)
    }
    private fun stageLocked(key: String, png: ByteArray) {
        validatePng(png); ensureRoot()
        val pending=File(root,"$key.pending")
        val retained=root.listFiles().orEmpty().filter {it!=pending}.sumOf {it.length()}
        require(retained+png.size<=MAX_CACHE_BYTES) { "Illustration cache budget exceeded" }
        require(root.listFiles().orEmpty().count {it.extension=="pending" || it.extension=="png"}<MAX_IMAGES || pending.exists()) { "Illustration count exceeded" }
        atomicWrite(pending,png)
    }
    /** Parent must explicitly review the staged bitmap. Cached art is character-free only. */
    fun approveStaged(key: String, parentReviewed: Boolean): Boolean = synchronized(lock) {
        worker(); validKey(key); require(parentReviewed)
        val pending=File(root,"$key.pending")
        if(!pending.isFile)return@synchronized false
        val bytes=boundedRead(pending) ?: return@synchronized false
        validatePng(bytes)
        val image=File(root,"$key.png")
        val manifest=JSONObject().put("version",1).put("sha256",digest(bytes)).put("bytes",bytes.size)
            .put("parentReviewed",true).toString().toByteArray(Charsets.UTF_8)
        // Account for the temporary duplicate during publication and manifest bytes.
        val retained=root.listFiles().orEmpty().filter {it!=image && it.name!="$key.json"}.sumOf {it.length()}
        require(retained+bytes.size+manifest.size<=MAX_CACHE_BYTES) { "Illustration cache budget exceeded" }
        atomicWrite(image,bytes)
        atomicWrite(File(root,"$key.json"),manifest)
        pending.delete()
        true
    }
    /** Replay-only lookup: never invokes capability(), authorBackground(), HTTP or bitmap pixel decoding. */
    fun replayFile(key: String): File? = synchronized(lock) {
        worker(); validKey(key)
        val image=File(root,"$key.png");val manifest=File(root,"$key.json")
        if(!image.isFile || !manifest.isFile || manifest.length()>1024)return@synchronized null
        runCatching {
            val metadata=JSONObject(manifest.readText(Charsets.UTF_8))
            require(metadata.getInt("version")==1 && metadata.getBoolean("parentReviewed"))
            val bytes=boundedRead(image) ?: return@synchronized null
            require(metadata.getInt("bytes")==bytes.size && metadata.getString("sha256")==digest(bytes))
            validatePng(bytes)
            image
        }.getOrNull()
    }
    fun remove(key:String) = synchronized(lock) {
        worker(); validKey(key)
        listOf("png","json","pending").forEach {File(root,"$key.$it").delete()}
    }
    private fun validatePng(bytes: ByteArray) {
        require(bytes.size in 33..MAX_IMAGE_BYTES && bytes.take(8).toByteArray().contentEquals(pngSignature)) { "PNG required" }
        val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true}
        BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
        require(bounds.outMimeType=="image/png" && bounds.outWidth in 1..MAX_DIMENSION && bounds.outHeight in 1..MAX_DIMENSION && bounds.outWidth.toLong()*bounds.outHeight<=MAX_PIXELS) { "Illustration dimensions invalid" }
    }
    private fun boundedRead(file:File):ByteArray? {
        if(file.length() !in 33..MAX_IMAGE_BYTES.toLong())return null
        return file.inputStream().use {stream->
            val out=java.io.ByteArrayOutputStream();val chunk=ByteArray(8192);var total=0
            while(true){val count=stream.read(chunk);if(count<0)break;total+=count;if(total>MAX_IMAGE_BYTES)return null;out.write(chunk,0,count)}
            out.toByteArray()
        }
    }
    private fun atomicWrite(file:File,bytes:ByteArray) {
        val atomic=AtomicFile(file);val stream=atomic.startWrite()
        try {stream.write(bytes);atomic.finishWrite(stream)} catch(failure:Throwable) {atomic.failWrite(stream);throw failure}
    }
    private fun ensureRoot(){check(root.isDirectory || root.mkdirs())}
    private fun validKey(key:String){require(key.matches(Regex("[a-z][a-z0-9-]{1,63}")))}
    private fun worker(){check(Looper.myLooper()!=Looper.getMainLooper()) {"Illustration I/O requires worker"}}
    private fun digest(bytes:ByteArray)=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") {"%02x".format(it)}
    companion object {
        private val lock=Any()
        const val MAX_IMAGE_BYTES=2*1024*1024
        const val MAX_CACHE_BYTES=16L*1024*1024
        const val MAX_IMAGES=24
        const val MAX_DIMENSION=1024
        const val MAX_PIXELS=1024L*1024
    }
}
