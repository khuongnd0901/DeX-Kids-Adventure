package com.khuongnd.dexkids.story

import android.app.*
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import com.khuongnd.dexkids.story.adventure.*
import java.util.concurrent.Executors

/** One process-wide downloader. Playback gates are held only by workers, never the UI. */
object StoryRemoteSync {
    private val download = Executors.newSingleThreadExecutor()
    private val gate = Any()
    private val leases = mutableSetOf<Any>()
    private var running = false
    private var lastAttempt = Long.MIN_VALUE
    private var generation = 0L
    private val listeners = mutableSetOf<() -> Unit>()
    private var client = StorySourceClient()
    data class Settings(val url:String,val enabled:Boolean,val lastSuccess:Long,val error:String)
    fun settings(context:Context):Settings = context.getSharedPreferences("story_source_v1",0).let {
        Settings(it.getString("url","") ?: "",it.getBoolean("enabled",false),it.getLong("success",0),it.getString("error","") ?: "")
    }
    fun configure(context:Context,url:String,enabled:Boolean) {
        val normalized=if(url.isBlank())"" else StorySourceClient.validateUrl(url)
        require(!enabled || normalized.isNotEmpty()) { "Cần URL HTTPS" }
        synchronized(this) {
            generation++;lastAttempt=Long.MIN_VALUE
            context.getSharedPreferences("story_source_v1",0).edit().putString("url",normalized).putBoolean("enabled",enabled).putLong("success",0).putString("error","").apply()
        }
        request(context,true)
    }
    fun addListener(listener:()->Unit) { synchronized(listeners) {listeners.add(listener)} }
    fun removeListener(listener:()->Unit) { synchronized(listeners) {listeners.remove(listener)} }
    private fun notifyChanged() { val copy=synchronized(listeners) {listeners.toList()};copy.forEach {runCatching {it()}} }
    /** Called on a story controller worker before reading an episode. */
    fun acquire(owner:Any) { synchronized(gate) {leases.add(owner)} }
    fun release(context:Context,owner:Any) {
        synchronized(gate) {
            leases.remove(owner)
            try {activateIfIdle(context)} catch(e:Exception) {
                context.getSharedPreferences("story_source_v1",0).edit().putString("error",errorMessage(e)).apply()
            }
        }
        notifyChanged()
    }
    private fun activateIfIdle(context:Context) {
        if(leases.isNotEmpty())return
        val config=settings(context)
        if(config.enabled && config.url.isNotEmpty()) StoryRepository(context).use {it.activateRemote(config.url)}
    }
    fun request(context:Context,force:Boolean=false) {
        val app=context.applicationContext
        val config=settings(app)
        if(!config.enabled || config.url.isEmpty()) {notifyChanged();return}
        val epoch:Long
        synchronized(this) {
            if(running)return
            val now=SystemClock.elapsedRealtime()
            if(!force && lastAttempt!=Long.MIN_VALUE && now-lastAttempt<300000)return
            running=true;lastAttempt=now;epoch=generation
        }
        download.execute {
            try {
                // Recover a staged pack after process death, including when the network is offline.
                // A staged snapshot that cannot activate must not prevent downloading a corrected pack.
                synchronized(gate) {runCatching {activateIfIdle(app)}}
                StoryRepository(app).use {repo ->
                    val cache=repo.remotePack(config.url)
                    val response=client.fetch(config.url,cache?.etag,cache?.modified)
                    val payload=if(response.unchanged) {
                        require(cache!=null) { "SOURCE_304_WITHOUT_CACHE" };cache.payload
                    } else {
                        StoryPackCodec.parse(response.body)
                        String(response.body,Charsets.UTF_8)
                    }
                    synchronized(gate) {
                        val current=settings(app)
                        if(synchronized(this) {epoch!=generation} || !current.enabled || current.url!=config.url)return@use
                        if(!response.unchanged) {
                            if(leases.isEmpty())repo.makeRemoteCacheRoom(config.url)
                            repo.stageRemote(config.url,payload,response.etag,response.modified,System.currentTimeMillis())
                        } else repo.checkedRemote(config.url,System.currentTimeMillis())
                        activateIfIdle(app)
                        synchronized(this) {
                            if(epoch==generation)app.getSharedPreferences("story_source_v1",0).edit().putLong("success",System.currentTimeMillis()).putString("error","").apply()
                        }
                    }
                }
            } catch(e:Exception) {
                synchronized(this) {
                    if(epoch==generation)app.getSharedPreferences("story_source_v1",0).edit().putString("error",errorMessage(e)).apply()
                }
            } finally {
                val changed=synchronized(this) {running=false;epoch!=generation}
                notifyChanged()
                if(changed)request(app,true)
            }
        }
    }
    private fun errorMessage(error:Exception):String = when(error) {
        is java.net.SocketTimeoutException -> "Hết thời gian tải. Tiếp tục dùng truyện đã lưu."
        is java.io.IOException -> "Không tải được nguồn truyện. Kiểm tra URL hoặc mạng; truyện đã lưu được giữ nguyên."
        else -> "Bộ truyện không hợp lệ hoặc vượt giới hạn. Giữ nguyên bộ đã lưu."
    }
}

/** Detect foreground across all app Activities, including direct child/library entry. */
class StorySourceApplication:Application(),Application.ActivityLifecycleCallbacks {
    private var started=0
    override fun onCreate() {super.onCreate();registerActivityLifecycleCallbacks(this)}
    override fun onActivityStarted(activity:Activity) {if(started++==0)StoryRemoteSync.request(this)}
    override fun onActivityStopped(activity:Activity) {started=(started-1).coerceAtLeast(0)}
    override fun onActivityCreated(activity:Activity,state:Bundle?) {}
    override fun onActivityResumed(activity:Activity) {}
    override fun onActivityPaused(activity:Activity) {}
    override fun onActivitySaveInstanceState(activity:Activity,state:Bundle) {}
    override fun onActivityDestroyed(activity:Activity) {}
}
