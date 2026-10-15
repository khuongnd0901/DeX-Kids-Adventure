package com.khuongnd.dexkids.story

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Looper
import com.khuongnd.dexkids.story.adventure.*

/** Private, bounded SQLite library. All methods require a worker; replay never invokes a gateway. */
class StoryRepository(context: Context) : SQLiteOpenHelper(context.applicationContext,"story-library-v1.db",null,1) {
    // Immutable validated content only; every read still obtains mutable review/delete flags from SQLite.
    // Full JSON keys avoid hash-only identity and naturally invalidate edited/replaced revisions.
    private val validatedEpisodes=object:LinkedHashMap<String,StoryEpisode>(64,0.75f,true) {
        override fun removeEldestEntry(eldest:MutableMap.MutableEntry<String,StoryEpisode>?):Boolean=size>64
    }
    private fun validated(json:String):StoryEpisode=synchronized(validatedEpisodes) {
        validatedEpisodes[json] ?: StoryEpisodeCodec.parse(json).also {validatedEpisodes[json]=it}
    }
    override fun close() {
        try {super.close()} finally {synchronized(validatedEpisodes) {validatedEpisodes.clear()}}
    }
    data class Entry(val episode: StoryEpisode, val reviewed: Boolean, val approved: Boolean,
                     val favourite: Boolean, val deleted: Boolean)
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE episode(id TEXT PRIMARY KEY,revision INTEGER NOT NULL,favourite INTEGER NOT NULL DEFAULT 0,deleted INTEGER NOT NULL DEFAULT 0,lastPlayed INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE TABLE revision(id TEXT NOT NULL,revision INTEGER NOT NULL,json TEXT NOT NULL,reviewed INTEGER NOT NULL,approved INTEGER NOT NULL,PRIMARY KEY(id,revision))")
        db.execSQL("CREATE TABLE playback(id TEXT PRIMARY KEY,revision INTEGER NOT NULL,beat INTEGER NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase,oldVersion: Int,newVersion: Int) { error("Unsupported story migration; preserve database") }
    private fun worker() { check(!Thread.currentThread().isInterrupted) { "Story operation cancelled" }; check(Looper.myLooper()!=Looper.getMainLooper()) { "Story storage requires worker" } }
    fun save(episode: StoryEpisode,reviewed: Boolean=false, expectedRevision:Int?=null): Entry {
        worker(); StoryEpisodeValidator.validate(episode)
        val json=StoryEpisodeCodec.toJson(episode); require(json.toByteArray().size<=65536)
        val db=writableDatabase; db.beginTransaction()
        try {
            require(android.database.DatabaseUtils.longForQuery(db,"SELECT COALESCE(SUM(length(CAST(json AS BLOB))),0) FROM revision",null)+json.toByteArray().size<=16*1024*1024) { "Thư viện đạt giới hạn 16 MiB" }
            val old=get(episode.id())
            require(expectedRevision==null || old?.episode?.revision()==expectedRevision) { "Story changed; reload" }
            if(old==null) require(android.database.DatabaseUtils.longForQuery(db,"SELECT COUNT(*) FROM episode",null)<200) { "Thư viện đầy (200 truyện)" }
            val revision=android.database.DatabaseUtils.longForQuery(db,"SELECT COALESCE(MAX(revision),0)+1 FROM revision WHERE id=?",arrayOf(episode.id())).toInt()
            require(revision<=100) { "Đã đạt giới hạn phiên bản" }
            val normalized=StoryEpisode(episode.id(),episode.schemaVersion(),revision,episode.title(),episode.audience(),episode.sauAge(),episode.ongAge(),episode.educationalGoal(),episode.synopsis(),episode.scenes())
            db.insertOrThrow("revision",null,ContentValues().apply { put("id",episode.id());put("revision",revision);put("json",StoryEpisodeCodec.toJson(normalized));put("reviewed",if(reviewed)1 else 0);put("approved",0) })
            if(old==null) db.insertOrThrow("episode",null,ContentValues().apply {put("id",episode.id());put("revision",revision)})
            else db.update("episode",ContentValues().apply {put("revision",revision);put("deleted",0)},"id=?",arrayOf(episode.id()))
            db.setTransactionSuccessful()
        } finally {db.endTransaction()}
        return get(episode.id())!!
    }
    fun get(id:String): Entry? { worker(); return readableDatabase.rawQuery("SELECT r.json,r.reviewed,r.approved,e.favourite,e.deleted FROM episode e JOIN revision r ON e.id=r.id AND e.revision=r.revision WHERE e.id=?",arrayOf(id)).use {c-> if(!c.moveToFirst())null else Entry(validated(c.getString(0)),c.getInt(1)==1,c.getInt(2)==1,c.getInt(3)==1,c.getInt(4)==1) } }
    fun list(includeDeleted:Boolean=false):List<Entry> {worker();val ids=readableDatabase.rawQuery("SELECT id FROM episode ${if(includeDeleted)"" else "WHERE deleted=0"} ORDER BY favourite DESC,lastPlayed ASC,id",null).use {c->buildList {while(c.moveToNext())add(c.getString(0))}};return ids.mapNotNull(::get)}
    fun setReviewed(id:String,revision:Int) {worker();writableDatabase.update("revision",ContentValues().apply {put("reviewed",1)},"id=? AND revision=?",arrayOf(id,revision.toString()))}
    fun approve(id:String,expectedRevision:Int) {worker();val entry=get(id)?:error("Missing story");require(entry.episode.revision()==expectedRevision);require(entry.reviewed && !entry.deleted);StoryEpisodeValidator.validate(entry.episode);writableDatabase.update("revision",ContentValues().apply {put("approved",1)},"id=? AND revision=?",arrayOf(id,entry.episode.revision().toString()))}
    fun favourite(id:String,value:Boolean) {worker();writableDatabase.update("episode",ContentValues().apply {put("favourite",if(value)1 else 0)},"id=?",arrayOf(id))}
    fun delete(id:String,value:Boolean=true) {worker();writableDatabase.update("episode",ContentValues().apply {put("deleted",if(value)1 else 0)},"id=?",arrayOf(id))}
    /** Explicit irreversible parent action; only already-deleted episodes may be purged. */
    fun purgeDeleted(id:String) {
        worker();val db=writableDatabase;db.beginTransaction()
        try {
            require(get(id)?.deleted==true) { "Only deleted stories may be permanently removed" }
            db.delete("playback","id=?",arrayOf(id))
            db.delete("revision","id=?",arrayOf(id))
            db.delete("episode","id=?",arrayOf(id))
            // SQLite reuses freed pages on future writes; no media files exist in sprite-only v1.
            db.setTransactionSuccessful()
        } finally {db.endTransaction()}
    }
    fun undo(id:String) {worker();val current=get(id)?:return;if(current.deleted){delete(id,false);return};require(current.episode.revision()>1);writableDatabase.update("episode",ContentValues().apply {put("revision",current.episode.revision()-1)},"id=?",arrayOf(id))}
    fun cursor(id:String,revision:Int,beat:Int) {worker();require(beat>=0);writableDatabase.insertWithOnConflict("playback",null,ContentValues().apply {put("id",id);put("revision",revision);put("beat",beat)},SQLiteDatabase.CONFLICT_REPLACE)}
    fun cursor(id:String,revision:Int):Int {worker();return readableDatabase.rawQuery("SELECT beat FROM playback WHERE id=? AND revision=?",arrayOf(id,revision.toString())).use {if(it.moveToFirst())it.getInt(0) else 0}}
    fun markPlayed(id:String) {worker();writableDatabase.update("episode",ContentValues().apply {put("lastPlayed",System.currentTimeMillis())},"id=?",arrayOf(id))}
    fun next(audience:StoryEpisode.Audience,seed:Long=System.nanoTime()):Entry? {
        worker();val candidates=list().filter {it.approved && it.episode.audience()==audience}
        if(candidates.isEmpty())return null
        val recent=readableDatabase.rawQuery("SELECT id FROM episode WHERE lastPlayed>0 ORDER BY lastPlayed DESC LIMIT 5",null).use {c->buildSet {while(c.moveToNext())add(c.getString(0))}}
        val eligible=candidates.filter {it.episode.id() !in recent}.ifEmpty {candidates}
        val unheardIds=readableDatabase.rawQuery("SELECT id FROM episode WHERE lastPlayed=0 AND deleted=0",null).use {c->buildSet {while(c.moveToNext())add(c.getString(0))}}
        val pool=eligible.filter {it.episode.id() in unheardIds}.ifEmpty {eligible}
        return pool[java.util.Random(seed).nextInt(pool.size)]
    }
}
