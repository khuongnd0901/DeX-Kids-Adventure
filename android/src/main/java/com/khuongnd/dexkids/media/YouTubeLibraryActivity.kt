package com.khuongnd.dexkids.media

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import com.khuongnd.dexkids.KidsSessionControl
import com.khuongnd.dexkids.story.EntertainmentDirector.Audience

/** Personal-use local links. Google account/Premium stays inside official YouTube. */
class YouTubeLibraryActivity : Activity() {
    private lateinit var store: YouTubeStore
    private var rows: List<YouTubeVideo> = emptyList()
    private var corrupt = false
    private var returnDialog: AlertDialog? = null
    override fun onCreate(state: Bundle?) { super.onCreate(state); store = YouTubeStore(this); render() }
    override fun onResume() {
        super.onResume()
        if (::store.isInitialized && store.pendingId.isNotEmpty() && returnDialog == null) {
            returnDialog = AlertDialog.Builder(this).setTitle("Quay lại từ YouTube")
                .setMessage("Hãy dừng video trong YouTube trước khi tiếp tục; Premium có thể vẫn phát nền.")
                .setCancelable(false).setPositiveButton("Tôi đã dừng YouTube") { _, _ ->
                    store.clearPending(); returnDialog = null; render()
                }.show()
        }
    }
    override fun onDestroy() { returnDialog?.dismiss(); returnDialog = null; super.onDestroy() }
    private fun render() {
        corrupt = false
        rows = try { store.videos() } catch (_: Exception) { corrupt = true; emptyList() }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = (20 * resources.displayMetrics.density).toInt(); setPadding(p,p,p,p)
        }
        fun label(text: String) { column.addView(TextView(this).apply { this.text = text; textSize = 18f }) }
        fun button(text: String, action: () -> Unit) {
            column.addView(Button(this).apply { this.text = text; isAllCaps = false; setOnClickListener { action() } })
        }
        label("YouTube · thư viện phụ huynh")
        label("Mở bằng YouTube chính thức trên DeX. Đăng nhập Premium trong YouTube để hưởng quyền lợi; ứng dụng này không kiểm tra thuê bao.\nTự xen kẽ sau 5 câu khi bật kể truyện. Trong lượt video, hành trình/GPS/POI riêng của app tạm dừng. Khi quay lại, cần xác nhận đã dừng YouTube. Bố cục do DeX/YouTube quản lý.")
        column.addView(CheckBox(this).apply {
            text = "Cho phép mở link đã duyệt bằng YouTube"; isChecked = store.enabled
            setOnCheckedChangeListener { _, checked ->
                if (!checked) { store.enabled = false; store.automatic = false; render() }
                else if (store.consented) { store.enabled = true; render() }
                else {
                    isChecked = false
                    AlertDialog.Builder(this@YouTubeLibraryActivity).setTitle("Trước khi bật YouTube")
                        .setMessage("Chỉ link video được gửi đến ứng dụng YouTube chính thức. Google/YouTube quản lý đăng nhập, lịch sử xem, quảng cáo, đề xuất và dữ liệu phát theo chính sách của họ; có thể có nội dung chưa được phụ huynh duyệt. Ứng dụng không gửi tên/tuổi, GPS, âm thanh hay câu trả lời của trẻ.\nYouTube có thể tiếp tục phát nền; phụ huynh cần dừng trước khi quay lại. Không tự nhận biết video kết thúc. Hành trình/GPS/POI riêng của app tạm dừng trong lượt YouTube; không thay đổi GPS hệ thống hay Maps/VietMap.\nĐiều khoản: https://www.youtube.com/t/terms\nRiêng tư Google: https://policies.google.com/privacy\nTắt tính năng hoặc xóa thư viện tại đây; lịch sử Google do bạn quản lý trong YouTube. Các lựa chọn của app không thay đổi tài khoản Google.")
                        .setNegativeButton("Chưa bật", null)
                        .setPositiveButton("Đã hiểu · Bật") { _, _ -> store.acceptNotice(); store.enabled = true; render() }.show()
                }
            }
        })
        column.addView(CheckBox(this).apply {
            text = "Tự mở video ở lượt xen kẽ"; isChecked = store.automatic; isEnabled = store.enabled
            setOnCheckedChangeListener { _, checked -> store.automatic = checked }
        })
        button("Tỷ lệ: 1 video mỗi ${store.every} lượt xen kẽ (mỗi lượt sau 5 câu)") {
            AlertDialog.Builder(this).setTitle("Tỷ lệ truyện / video")
                .setItems(arrayOf("1 truyện : 1 video", "3 truyện : 1 video")) { _, i -> store.every = if (i == 0) 2 else 4; render() }.show()
        }
        label("Tối đa 2 yêu cầu mở / giờ và 15 phút video dự kiến / giờ; không tự cắt video hay giới hạn hành trình. Thời lượng do phụ huynh nhập.")
        if (corrupt) label("Không đọc được thư viện. Không mở video. Bạn có thể xóa thư viện hỏng để tạo lại.")
        if (!corrupt) {
            for (video in rows) button("${if (video.approved() && video.enabled()) "✓" else "○"} ${video.title()} · ${video.audience()} · ${video.minAge()}–${video.maxAge()}") {
                AlertDialog.Builder(this).setTitle(video.title())
                    .setItems(arrayOf("Sửa / duyệt lại", "Xem thử bằng YouTube", "Xóa")) { _, i ->
                        when (i) {
                            0 -> edit(video)
                            1 -> preview(video)
                            2 -> AlertDialog.Builder(this).setTitle("Xóa link này?")
                                .setNegativeButton("Hủy", null).setPositiveButton("Xóa") { _, _ ->
                                    save(rows.filterNot { it.videoId() == video.videoId() })
                                }.show()
                        }
                    }.show()
            }
            if (rows.size < YouTubeLibrary.MAX_VIDEOS) button("Thêm link video") { edit(null) }
        }
        button("Xóa toàn bộ thư viện và tắt YouTube") {
            AlertDialog.Builder(this).setTitle("Xóa toàn bộ link đã lưu?")
                .setNegativeButton("Hủy", null).setPositiveButton("Xóa") { _, _ -> store.deleteLibrary(); render() }.show()
        }
        button("Quay lại") { finish() }
        setContentView(ScrollView(this).apply { addView(column) })
    }
    private fun save(videos: List<YouTubeVideo>) {
        try { store.save(videos); render() }
        catch (_: Exception) { message("Không lưu được. Bản đã lưu trước được giữ nguyên.") }
    }
    private fun edit(old: YouTubeVideo?) {
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24,16,24,16) }
        fun field(hint: String, text: String): EditText {
            return EditText(this).apply { this.hint = hint; setText(text); column.addView(this) }
        }
        val url = field("Link HTTPS YouTube hoặc ID video", old?.url().orEmpty()).apply {
            setSingleLine(true)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_URI or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            imeOptions = android.view.inputmethod.EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING or
                android.view.inputmethod.EditorInfo.IME_FLAG_FORCE_ASCII
        }
        column.addView(Button(this).apply {
            text = "Dán link/ID từ bộ nhớ tạm"; isAllCaps = false
            setOnClickListener {
                // Read clipboard only on this explicit parent action; no automatic access.
                val clipboard = getSystemService(android.content.ClipboardManager::class.java)
                val clip = clipboard.primaryClip
                val text = if (clip != null && clip.itemCount > 0) clip.getItemAt(0).text?.toString() else null
                try {
                    val id = YouTubeVideo.idFromInput(text)
                    url.setText("https://www.youtube.com/watch?v=$id")
                    url.setSelection(url.text.length); url.error = null
                } catch (_: IllegalArgumentException) { url.error = "Hãy sao chép một link hoặc ID video YouTube hợp lệ." }
            }
        })
        column.addView(TextView(this).apply {
            text = "Link/ID phân biệt chữ hoa thường. Nếu gõ tay, chuyển bàn phím sang English để tránh bộ gõ tiếng Việt đổi ký tự."
        })
        val title = field("Tên do phụ huynh đặt", old?.title().orEmpty())
        val audience = Spinner(this).apply {
            adapter = ArrayAdapter(this@YouTubeLibraryActivity, android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Cả Sâu và Ong", "Chỉ Sâu", "Chỉ Ong"))
            setSelection(when (old?.audience()) { Audience.SAU -> 1; Audience.ONG -> 2; else -> 0 }); column.addView(this)
        }
        val min = field("Tuổi nhỏ nhất (4–7)", (old?.minAge() ?: 4).toString())
        val max = field("Tuổi lớn nhất (4–7)", (old?.maxAge() ?: 7).toString())
        val minutes = field("Thời lượng dự kiến, phút (1–30)", ((old?.plannedSeconds() ?: 300) / 60).coerceAtLeast(1).toString())
        listOf(min,max,minutes).forEach { it.inputType = android.text.InputType.TYPE_CLASS_NUMBER }
        val recall = field("Tùy chọn: tối đa 2 câu hỏi đã duyệt, mỗi dòng 1 câu", old?.recall().orEmpty())
        val approved = CheckBox(this).apply { text = "Tôi đã xem và duyệt toàn bộ video cho người xem / độ tuổi ở trên"; column.addView(this) }
        val enabled = CheckBox(this).apply { text = "Dùng link này trong các lượt video"; isChecked = old?.enabled() ?: true; column.addView(this) }
        val dialog = AlertDialog.Builder(this).setTitle("Link do phụ huynh chọn · không tự nhập playlist")
            .setView(ScrollView(this).apply { addView(column) }).setNegativeButton("Hủy", null).setPositiveButton("Lưu", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                try {
                    val video = YouTubeVideo(YouTubeVideo.idFromInput(url.text.toString()), title.text.toString().trim(),
                        arrayOf(Audience.BOTH,Audience.SAU,Audience.ONG)[audience.selectedItemPosition],
                        min.text.toString().toInt(), max.text.toString().toInt(), Math.multiplyExact(minutes.text.toString().toInt(),60),
                        approved.isChecked, enabled.isChecked, recall.text.toString().trim())
                    require(rows.none { it.videoId() == video.videoId() && it.videoId() != old?.videoId() })
                    store.save(rows.filterNot { it.videoId() == old?.videoId() } + video)
                    dialog.dismiss(); render()
                } catch (_: Exception) { url.error = "Kiểm tra link video, tên, tuổi 4–7, phút 1–30, tối đa 2 câu hỏi / 400 ký tự; không trùng ID." }
            }
        }; dialog.show()
    }
    private fun preview(video: YouTubeVideo) {
        if (KidsSessionControl.active()) { message("Hãy kết thúc hành trình trước khi xem thử từ thư viện."); return }
        if (!store.enabled || !video.approved() || !video.enabled()) { message("Cần bật YouTube và duyệt link trước."); return }
        try {
            OfficialYouTube.preflight(this,video)?.let { message(it); return }
            store.begin(video)
            OfficialYouTube.launch(this,video)
        } catch (_: Exception) { store.clearPending(); message("Không mở được YouTube trên DeX; không mở trên điện thoại.") }
    }
    private fun message(text: String) { AlertDialog.Builder(this).setMessage(text).setPositiveButton("Đóng",null).show() }
}
