package com.khuongnd.dexkids.story

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Looper
import com.khuongnd.dexkids.story.adventure.*

/** Private, bounded SQLite library. All methods require a worker; replay never invokes a gateway. */
class StoryRepository(private val context: Context) : SQLiteOpenHelper(context.applicationContext,"story-library-v1.db",null,2) {
    // Immutable validated content only; every read still obtains mutable review/delete flags from SQLite.
    // Full JSON keys avoid hash-only identity and naturally invalidate edited/replaced revisions.
    private val validatedEpisodes=object:LinkedHashMap<String,StoryEpisode>(64,0.75f,true) {
        override fun removeEldestEntry(eldest:MutableMap.MutableEntry<String,StoryEpisode>?):Boolean=size>64
    }
    private fun validated(json:String,scan:Map<String,StoryEpisode> = emptyMap()):StoryEpisode=synchronized(validatedEpisodes) {
        (validatedEpisodes[json] ?: scan[json] ?: StoryEpisodeCodec.parse(json)).also {validatedEpisodes[json]=it}
    }
    override fun close() {
        try {super.close()} finally {synchronized(validatedEpisodes) {validatedEpisodes.clear()}}
    }
    data class Entry(val episode: StoryEpisode, val reviewed: Boolean, val approved: Boolean,
                     val favourite: Boolean, val deleted: Boolean, val remote: Boolean = false)
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE episode(id TEXT PRIMARY KEY,revision INTEGER NOT NULL,favourite INTEGER NOT NULL DEFAULT 0,deleted INTEGER NOT NULL DEFAULT 0,lastPlayed INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE TABLE revision(id TEXT NOT NULL,revision INTEGER NOT NULL,json TEXT NOT NULL,reviewed INTEGER NOT NULL,approved INTEGER NOT NULL,PRIMARY KEY(id,revision))")
        db.execSQL("CREATE TABLE playback(id TEXT PRIMARY KEY,revision INTEGER NOT NULL,beat INTEGER NOT NULL)")
        createRemote(db)
    }
    override fun onUpgrade(db: SQLiteDatabase,oldVersion: Int,newVersion: Int) {
        check(oldVersion == 1 && newVersion == 2) { "Unsupported story migration; preserve database" }
        createRemote(db)
    }
    private fun createRemote(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE remote_pack(source TEXT PRIMARY KEY,payload TEXT NOT NULL,etag TEXT,modified TEXT,checked INTEGER NOT NULL,applied INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE TABLE remote_episode(id TEXT PRIMARY KEY,source TEXT NOT NULL,originalId TEXT NOT NULL,revision INTEGER NOT NULL,json TEXT NOT NULL,fingerprint TEXT NOT NULL,available INTEGER NOT NULL DEFAULT 1,favourite INTEGER NOT NULL DEFAULT 0,lastPlayed INTEGER NOT NULL DEFAULT 0,beat INTEGER NOT NULL DEFAULT 0)")
    }
    private fun localOnly(id:String) { require(remoteGet(id)==null) { "Truyện nguồn ngoài chỉ đọc; hãy sao chép thành bản cục bộ" } }

    private fun worker() { check(!Thread.currentThread().isInterrupted) { "Story operation cancelled" }; check(Looper.myLooper()!=Looper.getMainLooper()) { "Story storage requires worker" } }
    fun save(episode: StoryEpisode,reviewed: Boolean=false, expectedRevision:Int?=null): Entry {
        worker(); localOnly(episode.id()); StoryEpisodeValidator.validate(episode)
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
    fun get(id:String): Entry? { worker(); remoteGet(id)?.let { return it }; return readableDatabase.rawQuery("SELECT r.json,r.reviewed,r.approved,e.favourite,e.deleted FROM episode e JOIN revision r ON e.id=r.id AND e.revision=r.revision WHERE e.id=?",arrayOf(id)).use {c-> if(!c.moveToFirst())null else Entry(validated(c.getString(0)),c.getInt(1)==1,c.getInt(2)==1,c.getInt(3)==1,c.getInt(4)==1) } }
    fun list(includeDeleted:Boolean=false):List<Entry> {
        worker()
        // Freeze up to64 references during a scan: an oversized list must not evict
        // the later cache hits before they are read. Persistent capacity stays64.
        val scan=synchronized(validatedEpisodes) {HashMap(validatedEpisodes)}
        // One cursor per source partition; metadata stays live even when validated JSON is cached.
        val local=readableDatabase.rawQuery("SELECT r.json,r.reviewed,r.approved,e.favourite,e.deleted FROM episode e JOIN revision r ON e.id=r.id AND e.revision=r.revision ${if(includeDeleted)"" else "WHERE e.deleted=0"} ORDER BY e.favourite DESC,e.lastPlayed ASC,e.id",null).use {c->
            buildList {while(c.moveToNext())add(Entry(validated(c.getString(0),scan),c.getInt(1)==1,c.getInt(2)==1,c.getInt(3)==1,c.getInt(4)==1))}
        }
        return local + remoteList(includeDeleted,scan)
    }
    fun setReviewed(id:String,revision:Int) {worker();localOnly(id);writableDatabase.update("revision",ContentValues().apply {put("reviewed",1)},"id=? AND revision=?",arrayOf(id,revision.toString()))}
    fun approve(id:String,expectedRevision:Int) {worker();localOnly(id);val entry=get(id)?:error("Missing story");require(entry.episode.revision()==expectedRevision);require(entry.reviewed && !entry.deleted);StoryEpisodeValidator.validate(entry.episode);writableDatabase.update("revision",ContentValues().apply {put("approved",1)},"id=? AND revision=?",arrayOf(id,entry.episode.revision().toString()))}
    fun favourite(id:String,value:Boolean) {worker();writableDatabase.update(if(remoteGet(id)!=null)"remote_episode" else "episode",ContentValues().apply {put("favourite",if(value)1 else 0)},"id=?",arrayOf(id))}
    fun delete(id:String,value:Boolean=true) {worker();localOnly(id);writableDatabase.update("episode",ContentValues().apply {put("deleted",if(value)1 else 0)},"id=?",arrayOf(id))}
    /** Explicit irreversible parent action; only already-deleted episodes may be purged. */
    fun purgeDeleted(id:String) {
        worker();localOnly(id);val db=writableDatabase;db.beginTransaction()
        try {
            require(get(id)?.deleted==true) { "Only deleted stories may be permanently removed" }
            db.delete("playback","id=?",arrayOf(id))
            db.delete("revision","id=?",arrayOf(id))
            db.delete("episode","id=?",arrayOf(id))
            // SQLite reuses freed pages on future writes; no media files exist in sprite-only v1.
            db.setTransactionSuccessful()
        } finally {db.endTransaction()}
    }
    fun undo(id:String) {worker();localOnly(id);val current=get(id)?:return;if(current.deleted){delete(id,false);return};require(current.episode.revision()>1);writableDatabase.update("episode",ContentValues().apply {put("revision",current.episode.revision()-1)},"id=?",arrayOf(id))}
    fun cursor(id:String,revision:Int,beat:Int) {worker();require(beat>=0);
        if(remoteGet(id)!=null) {writableDatabase.update("remote_episode",ContentValues().apply {put("beat",beat)},"id=? AND revision=?",arrayOf(id,revision.toString()));return}
writableDatabase.insertWithOnConflict("playback",null,ContentValues().apply {put("id",id);put("revision",revision);put("beat",beat)},SQLiteDatabase.CONFLICT_REPLACE)}
    fun cursor(id:String,revision:Int):Int {worker();
        if(remoteGet(id)!=null)return readableDatabase.rawQuery("SELECT beat FROM remote_episode WHERE id=? AND revision=?",arrayOf(id,revision.toString())).use {if(it.moveToFirst())it.getInt(0) else 0}
        return readableDatabase.rawQuery("SELECT beat FROM playback WHERE id=? AND revision=?",arrayOf(id,revision.toString())).use {if(it.moveToFirst())it.getInt(0) else 0}}
    fun markPlayed(id:String) {worker();writableDatabase.update(if(remoteGet(id)!=null)"remote_episode" else "episode",ContentValues().apply {put("lastPlayed",System.currentTimeMillis())},"id=?",arrayOf(id))}
    fun next(audience:StoryEpisode.Audience,seed:Long=System.nanoTime()):Entry? {
        worker();val candidates=list().filter {it.approved && it.episode.audience()==audience}
        if(candidates.isEmpty())return null
        val recent=readableDatabase.rawQuery("SELECT id FROM (SELECT id,lastPlayed FROM episode WHERE lastPlayed>0 UNION ALL SELECT id,lastPlayed FROM remote_episode WHERE lastPlayed>0 AND available=1 AND source=?) ORDER BY lastPlayed DESC LIMIT 5",arrayOf(remoteSource())).use {c->buildSet {while(c.moveToNext())add(c.getString(0))}}
        val eligible=candidates.filter {it.episode.id() !in recent}.ifEmpty {candidates}
        val unheardIds=readableDatabase.rawQuery("SELECT id FROM episode WHERE lastPlayed=0 AND deleted=0 UNION ALL SELECT id FROM remote_episode WHERE lastPlayed=0 AND available=1 AND source=?",arrayOf(remoteSource())).use {c->buildSet {while(c.moveToNext())add(c.getString(0))}}
        val pool=eligible.filter {it.episode.id() in unheardIds}.ifEmpty {eligible}
        return pool[java.util.Random(seed).nextInt(pool.size)]
    }

    private fun remoteSource():String = context.getSharedPreferences("story_source_v1",Context.MODE_PRIVATE).let {
        if(it.getBoolean("enabled",false))it.getString("url","") ?: "" else ""
    }
    fun selectable(id:String):Boolean {worker();return remoteGet(id)?.let { !it.deleted && readableDatabase.rawQuery("SELECT source FROM remote_episode WHERE id=?",arrayOf(id)).use {c->c.moveToFirst() && c.getString(0)==remoteSource()} } ?: (get(id)?.let {!it.deleted} ?: false)}
    private fun remoteGet(id:String):Entry? = readableDatabase.rawQuery("SELECT json,available,favourite FROM remote_episode WHERE id=?",arrayOf(id)).use {c->
        if(!c.moveToFirst())null else Entry(validated(c.getString(0)),false,true,c.getInt(2)==1,c.getInt(1)==0,true)
    }
    private fun remoteList(includeDeleted:Boolean,scan:Map<String,StoryEpisode>):List<Entry> = readableDatabase.rawQuery("SELECT json,available,favourite FROM remote_episode WHERE source=? ${if(includeDeleted)"" else "AND available=1"} ORDER BY favourite DESC,lastPlayed,id",arrayOf(remoteSource())).use {c->
        buildList {while(c.moveToNext())add(Entry(validated(c.getString(0),scan),false,true,c.getInt(2)==1,c.getInt(1)==0,true))}
    }
    data class RemotePack(val payload:String,val etag:String?,val modified:String?,val checked:Long,val applied:Boolean)
    fun remotePack(source:String):RemotePack? {worker();return readableDatabase.rawQuery("SELECT payload,etag,modified,checked,applied FROM remote_pack WHERE source=?",arrayOf(source)).use {c->if(!c.moveToFirst())null else RemotePack(c.getString(0),c.getString(1),c.getString(2),c.getLong(3),c.getInt(4)==1)}}
    /** Caller validates the complete bounded pack before staging; revalidation guards other callers. */
    fun stageRemote(source:String,payload:String,etag:String?,modified:String?,checked:Long) {
        worker();StoryPackCodec.parse(payload.toByteArray(Charsets.UTF_8))
        require(remotePack(source)!=null || android.database.DatabaseUtils.longForQuery(readableDatabase,"SELECT COUNT(*) FROM remote_pack",null)<3) { "Đang phát truyện; hãy thử lại sau để đổi nguồn" }
        writableDatabase.insertWithOnConflict("remote_pack",null,ContentValues().apply {
            put("source",source);put("payload",payload);put("etag",etag);put("modified",modified);put("checked",checked);put("applied",0)
        },SQLiteDatabase.CONFLICT_REPLACE)
    }
    fun checkedRemote(source:String,checked:Long) {worker();writableDatabase.update("remote_pack",ContentValues().apply {put("checked",checked)},"source=?",arrayOf(source))}
    /** Must run under the coordinator's playback gate. One transaction replaces the visible snapshot. */
    fun activateRemote(source:String) {
        worker();val pack=remotePack(source) ?: return
        if(pack.applied)return
        val episodes=StoryPackCodec.parse(pack.payload.toByteArray(Charsets.UTF_8))
        val db=writableDatabase;db.beginTransaction()
        try {
            db.update("remote_episode",ContentValues().apply {put("available",0)},"source=?",arrayOf(source))
            for(e in episodes) {
                val id=StoryPackCodec.remoteId(source,e.id())
                require(android.database.DatabaseUtils.longForQuery(db,"SELECT COUNT(*) FROM episode WHERE id=?",arrayOf(id))==0L) { "Remote identity conflicts with local story" }
                val fingerprint=StoryPackCodec.digest(StoryEpisodeCodec.toJson(StoryPackCodec.identity(e,e.id(),1)))
                val old=db.rawQuery("SELECT revision,fingerprint FROM remote_episode WHERE id=?",arrayOf(id)).use {c->if(c.moveToFirst())Pair(c.getInt(0),c.getString(1)) else null}
                val changed=old==null || old.second!=fingerprint
                val revision=if(old==null)1 else old.first+if(changed)1 else 0
                require(revision<=100000) { "Giới hạn phiên bản nguồn" }
                val json=StoryEpisodeCodec.toJson(StoryPackCodec.identity(e,id,revision))
                val values=ContentValues().apply {put("source",source);put("originalId",e.id());put("revision",revision);put("json",json);put("fingerprint",fingerprint);put("available",1);if(changed)put("beat",0)}
                if(old==null){values.put("id",id);db.insertOrThrow("remote_episode",null,values)} else db.update("remote_episode",values,"id=?",arrayOf(id))
            }
            // Removed stories have no live leases and can now be discarded, bounding storage.
            db.delete("remote_episode","source=? AND available=0",arrayOf(source))
            db.update("remote_pack",ContentValues().apply {put("applied",1)},"source=?",arrayOf(source))
            // Retain at most three source caches. Never evict while playback leases exist.
            val stale=db.rawQuery("SELECT source FROM remote_pack WHERE source<>? ORDER BY checked DESC LIMIT -1 OFFSET 2",arrayOf(source)).use {c->buildList {while(c.moveToNext())add(c.getString(0))}}
            stale.forEach {db.delete("remote_episode","source=?",arrayOf(it));db.delete("remote_pack","source=?",arrayOf(it))}
            db.setTransactionSuccessful()
        } finally {db.endTransaction()}
    }
    fun makeRemoteCacheRoom(source:String) {
        worker();val db=writableDatabase
        val stale=db.rawQuery("SELECT source FROM remote_pack WHERE source<>? ORDER BY checked DESC LIMIT -1 OFFSET 2",arrayOf(source)).use {c->buildList {while(c.moveToNext())add(c.getString(0))}}
        db.beginTransaction()
        try {stale.forEach {db.delete("remote_episode","source=?",arrayOf(it));db.delete("remote_pack","source=?",arrayOf(it))};db.setTransactionSuccessful()} finally {db.endTransaction()}
    }
    fun copyRemote(id:String):Entry {
        worker();val entry=remoteGet(id) ?: error("Missing remote story")
        return save(StoryPackCodec.identity(entry.episode,"copy-"+java.util.UUID.randomUUID().toString(),1))
    }
}
