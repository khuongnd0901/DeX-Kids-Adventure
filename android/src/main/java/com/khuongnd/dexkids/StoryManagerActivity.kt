package com.khuongnd.dexkids

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.content.Intent
import android.widget.*
import com.khuongnd.dexkids.story.*
import com.khuongnd.dexkids.story.adventure.*
import java.util.concurrent.Executors

/** Adult-owned library: all DB, parsing, validation and AI work happens on one cancellable worker. */
class StoryManagerActivity:Activity() {
    private val worker=Executors.newSingleThreadExecutor()
    private lateinit var repository:StoryRepository
    private lateinit var column:LinearLayout
    private lateinit var status:TextView
    @Volatile private var busy=false
    private var pending:java.util.concurrent.Future<*>?=null
    private var query=""
    private var audienceFilter:StoryEpisode.Audience?=null
    override fun onCreate(state:Bundle?) {super.onCreate(state);repository=StoryRepository(this);render()}
    private fun button(label:String,action:()->Unit) {column.addView(Button(this).apply {text=label;isEnabled=!busy;setOnClickListener {action()}})}
    private fun render() {
        column=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;setPadding(48,20,48,20)}
        column.addView(TextView(this).apply {text="Thư viện truyện · phụ huynh duyệt trước khi phát";textSize=25f})
        status=TextView(this);column.addView(status)
        button("Tạo truyện AI theo chủ đề") {create()}
        button("Thêm truyện mẫu offline đã biên soạn") {chooseAudience {a->runWork {repository.save(StoryFixtures.educational(a),true);refresh()}}}
        val prefs=getSharedPreferences("story_manager_v1",MODE_PRIVATE)
        column.addView(CheckBox(this).apply {text="Tự động kể truyện đã duyệt (mặc định tắt)";isChecked=prefs.getBoolean("autoplay",false);setOnCheckedChangeListener {_,checked->prefs.edit().putBoolean("autoplay",checked).apply()}})
        button("Xem truyện đã xóa / hoàn tác") {runWork {val deleted=repository.list(true).filter {it.deleted};runOnUiThread {AlertDialog.Builder(this).setTitle("Khôi phục truyện").setItems(deleted.map {it.episode.title()}.toTypedArray()) {_,i->
            val id=deleted[i].episode.id()
            AlertDialog.Builder(this).setTitle(deleted[i].episode.title()).setItems(arrayOf("Khôi phục","Xóa vĩnh viễn")) {_,action->
                if(action==0)runWork {repository.delete(id,false);refresh()}
                else AlertDialog.Builder(this).setTitle("Xóa vĩnh viễn?").setMessage("Xóa mọi phiên bản và trạng thái phát. Không thể hoàn tác.").setNegativeButton("Hủy",null).setPositiveButton("Xóa vĩnh viễn") {_,_->runWork {repository.purgeDeleted(id);refresh()}}.show()
            }.setNegativeButton("Đóng",null).show()
        }.setNegativeButton("Đóng",null).show()}}}
        button("Tìm truyện") {
            val input=EditText(this).apply {setSingleLine();setText(query);hint="Tiêu đề hoặc mục tiêu"}
            AlertDialog.Builder(this).setTitle("Tìm trong thư viện").setView(input).setNegativeButton("Hủy",null).setPositiveButton("Tìm") {_,_->query=input.text.toString().trim();runWork {refresh()}}.show()
        }
        button("Lọc người xem") {AlertDialog.Builder(this).setTitle("Người xem").setItems(arrayOf("Tất cả","Sâu","Ong","Cả hai")) {_,i->audienceFilter=if(i==0)null else StoryEpisode.Audience.values()[i-1];runWork {refresh()}}.show()}
        column.addView(Button(this).apply {text="Hủy tác vụ đang chạy";setOnClickListener {pending?.cancel(true);status.text="Đã yêu cầu hủy. Thư viện đã lưu được giữ nguyên."}})
        button("Trở lại") {finish()}
        setContentView(ScrollView(this).apply {addView(column)})
        runWork {refresh()}
    }
    private fun runWork(action:()->Unit) {
        if(busy)return
        busy=true;status.text="Đang xử lý… Không đóng ứng dụng khi đang lưu."
        pending=worker.submit {try {action()} catch(e:Exception) {runOnUiThread {if(!isDestroyed)status.text="Chưa hoàn tất. Kiểm tra nội dung, quyền AI hoặc kết nối. Thư viện offline được giữ nguyên."}} finally {busy=false}}
    }
    private fun refresh() {
        val entries=repository.list().filter {(audienceFilter==null || it.episode.audience()==audienceFilter) && (it.episode.title()+" "+it.episode.educationalGoal()).contains(query,ignoreCase=true)}
        runOnUiThread {if(!isDestroyed) {
            while(column.childCount>10)column.removeViewAt(10)
            status.text="${entries.size} truyện · Phát lại offline không gọi AI · Hình minh họa dùng sprite có sẵn"
            entries.forEach {entry->column.addView(Button(this).apply {text=(if(entry.favourite)"★ " else "")+entry.episode.title()+" · "+entry.episode.audience()+" · v"+entry.episode.revision()+if(entry.approved)" · ĐÃ DUYỆT" else " · BẢN NHÁP";setOnClickListener {detail(entry)}})}
        }}
    }
    private fun chooseAudience(action:(StoryEpisode.Audience)->Unit) {AlertDialog.Builder(this).setTitle("Nhân vật").setItems(arrayOf("Sâu · 5 tuổi","Ong · 4 tuổi","Cả hai · học theo tuổi 4")) {_,i->action(StoryEpisode.Audience.values()[i])}.show()}
    private fun create() {chooseAudience {audience->
        val topic=EditText(this).apply {hint="Chủ đề chung, ví dụ giúp bạn";setSingleLine()}
        val goal=EditText(this).apply {hint="Mục tiêu, ví dụ biết chia sẻ";setSingleLine()}
        val form=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;addView(topic);addView(goal)}
        AlertDialog.Builder(this).setTitle("Tạo bản nháp AI · 2 lượt API").setMessage("Cần bật AI và quyền sáng tạo trong cài đặt AI. Chỉ nhập chủ đề chung, không nhập tên thật, GPS hay thông tin trẻ. Có thể dùng quota nhà cung cấp; không tạo ảnh. AI duyệt độc lập rồi phụ huynh duyệt.").setView(form).setNegativeButton("Hủy",null).setPositiveButton("Tạo") {_,_->val t=topic.text.toString().trim();val g=goal.text.toString().trim();runWork {repository.save(StoryAuthor(this).generate(t,g,audience),true);refresh()}}.show()
    }}
    private fun detail(entry:StoryRepository.Entry) {
        val e=entry.episode
        val preview=e.title()+"\n"+e.educationalGoal()+"\n"+e.synopsis()+"\n\n"+e.scenes().joinToString("\n\n") {s->s.id()+" · "+s.backdrop()+"\n"+s.beats().joinToString("\n") {b->"${b.actor()}: ${b.utterance()} [${b.action()} / ${b.emotion()} / ${b.prop()}]"}+(s.question()?.let {"\nHỏi ${it.target()}: ${it.prompt()}\nGợi ý: ${it.hint()}\nPhản hồi: ${it.feedback()}\nIm lặng: ${it.silenceFallback()}"}?:"")}
        AlertDialog.Builder(this).setTitle("Xem trước · v${e.revision()}").setView(dialogViewport(TextView(this).apply {text=preview;textSize=18f;setPadding(28,16,28,16)})).setNegativeButton("Đóng",null).setPositiveButton("Thao tác") {_,_->
            val options=arrayOf("Sửa lời kể / câu hỏi","Duyệt để sử dụng","Phát lại offline","Yêu thích / bỏ thích","Hoàn tác phiên bản","Xóa truyện","AI duyệt lại bản nháp","AI viết lại một cảnh")
            AlertDialog.Builder(this).setTitle(e.title()).setItems(options) {_,i->when(i) {
                0->edit(entry)
                1->AlertDialog.Builder(this).setTitle("Phụ huynh đã đọc và đồng ý?").setMessage("AI có thể sai. Bạn xác nhận toàn bộ nội dung, câu hỏi và hành động phù hợp cho trẻ?").setNegativeButton("Hủy",null).setPositiveButton("Duyệt") {_,_->runWork {repository.approve(e.id(),e.revision());refresh()}}.show()
                2->if(entry.approved) {ParentSettings(this).audienceMode=e.audience().name;if(callingActivity!=null) {setResult(RESULT_OK,Intent().putExtra("story_episode_id",e.id()));finish()} else startActivity(Intent(this,KidsActivity::class.java).putExtra("story_episode_id",e.id()))} else status.text="Cần duyệt bản nháp trước khi phát."
                3->runWork {repository.favourite(e.id(),!entry.favourite);refresh()}
                4->runWork {repository.undo(e.id());refresh()}
                7->AlertDialog.Builder(this).setTitle("Viết lại cảnh · 2 lượt API").setMessage("Tạo nội dung và duyệt độc lập, dùng quota AI đã bật. Lưu bản nháp mới cần phụ huynh duyệt; không gọi tạo ảnh.").setItems(e.scenes().map {it.id()}.toTypedArray()) {_,sceneIndex->runWork {val regenerated=StoryAuthor(this).regenerateScene(e,sceneIndex);repository.save(regenerated,true,e.revision());refresh()}}.setNegativeButton("Hủy",null).show()
                6->runWork {StoryAuthor(this).review(e);repository.setReviewed(e.id(),e.revision());refresh()}
                5->AlertDialog.Builder(this).setTitle("Xóa truyện?").setMessage("Có thể khôi phục trong mục truyện đã xóa.").setNegativeButton("Hủy",null).setPositiveButton("Xóa") {_,_->runWork {repository.delete(e.id());refresh()}}.show()
            }}.setNegativeButton("Đóng",null).show()
        }.show()
    }
    private fun edit(entry:StoryRepository.Entry) {
        val e=entry.episode
        val labels=mutableListOf<String>();val fields=mutableListOf<EditText>();val form=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;setPadding(24,12,24,12)}
        e.scenes().forEach {s->s.beats().forEach {b->labels.add(s.id()+" · "+b.actor());fields.add(EditText(this).apply {setText(b.utterance())})};s.question()?.let {q->listOf("Câu hỏi" to q.prompt(),"Ý trả lời (ngăn bằng ;)" to q.acceptedConcepts().joinToString(";"),"Gợi ý" to q.hint(),"Phản hồi" to q.feedback(),"Khi im lặng" to q.silenceFallback()).forEach {(label,value)->labels.add(s.id()+" · "+label);fields.add(EditText(this).apply {setText(value)})}}}
        fields.forEachIndexed {i,f->form.addView(TextView(this).apply {text=labels[i]});form.addView(f)}
        AlertDialog.Builder(this).setTitle("Sửa nội dung · tạo bản nháp mới").setMessage("Sau sửa, cue được dựng lại ở chế độ an toàn và hủy duyệt cũ. Nội dung sửa cần AI duyệt lại và phụ huynh duyệt.").setView(dialogViewport(form)).setNegativeButton("Hủy",null).setPositiveButton("Lưu và duyệt lại AI") {_,_->
            val values=fields.map {it.text.toString().trim()}
            runWork {
                var index=0
                val scenes=e.scenes().map {s->StoryEpisode.Scene(s.id(),VisualCue.Theme.GENERAL,s.beats().map {b->values[index++].let {text->val cue=VisualCue.fromAuthored("",text,"");StoryEpisode.Beat(b.actor(),text,b.emotion(),editedAction(text,cue.action()),cue.prop())}},s.question()?.let {q->StoryEpisode.Question(q.target(),values[index++],values[index++].split(";").map {it.trim()},values[index++],values[index++],values[index++],q.maxWaitSeconds())})}
                val edited=StoryEpisode(e.id(),1,e.revision(),e.title(),e.audience(),5,4,e.educationalGoal(),e.synopsis(),scenes)
                // Save first so an unavailable reviewer cannot lose the parent's work.
                val saved=repository.save(edited,false,e.revision())
                try {StoryAuthor(this).review(saved.episode);repository.setReviewed(saved.episode.id(),saved.episode.revision())} finally {refresh()}
            }
        }.show()
    }
    /** Bound the inner viewport: AlertDialog can replace layout params on the outer custom view. */
    private fun dialogViewport(content:android.view.View):FrameLayout = FrameLayout(this).apply {
        val viewport=ScrollView(this@StoryManagerActivity).apply {addView(content)}
        addView(viewport,FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
            (resources.displayMetrics.heightPixels*0.55f).toInt()))
    }
    private fun editedAction(text:String,fallback:VisualCue.Action):VisualCue.Action {
        val folded=java.text.Normalizer.normalize(text.lowercase().replace('đ','d'),java.text.Normalizer.Form.NFD).replace(Regex("\\p{M}+"),"")
        return when {folded.contains("vay tay")->VisualCue.Action.WAVE;folded.contains("nguoc nhin")->VisualCue.Action.LOOK_UP;folded.contains("vuot ve")->VisualCue.Action.PET;else->fallback}
    }
    override fun onDestroy() {
        worker.shutdownNow()
        Thread({
            try {while(!worker.awaitTermination(5,java.util.concurrent.TimeUnit.SECONDS)) { /* Wait off UI until HTTP timeout/cancellation settles. */ }}
            finally {repository.close()}
        },"story-library-close").start()
        super.onDestroy()
    }
}
