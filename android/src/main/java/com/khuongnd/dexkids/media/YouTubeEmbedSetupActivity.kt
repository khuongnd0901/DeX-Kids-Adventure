package com.khuongnd.dexkids.media

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.widget.*

class YouTubeEmbedSetupActivity : Activity() {
    override fun onCreate(state: Bundle?) { super.onCreate(state); render() }
    private fun render() {
        val settings = YouTubeEmbedSettings(this)
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28,20,28,20) }
        fun label(text: String) = TextView(this).also { it.text = text; it.textSize = 17f; column.addView(it) }
        fun field(hint: String, value: String) = EditText(this).also {
            it.hint = hint; it.setText(value); it.setSingleLine(true); column.addView(it)
        }
        fun button(text: String, action: () -> Unit) { column.addView(Button(this).apply {
            this.text = text; isAllCaps = false; setOnClickListener { action() }
        }) }
        label("YouTube trong app · cấu hình phụ huynh")
        label(if (settings.ready()) "Đã cấu hình dùng cá nhân. Không phải xác nhận tuân thủ của Google."
            else "Chưa sẵn sàng. App dùng truyện offline; chưa gọi API hoặc tải trình phát.")
        label("1. Tạo Google Cloud project, bật YouTube Data API v3; giới hạn key cho API này và ứng dụng Android đúng package/chứng thư ký.\n2. Chỉ dùng APK cá nhân/gia đình; phụ huynh duyệt từng video. App không tự gửi khai báo Google. Dùng riêng không đồng nghĩa được miễn chính sách YouTube.\n3. Đọc và đồng ý thông tin quyền riêng tư trước khi bật phát.")
        label("Hướng dẫn Google và chính sách:").apply {
            text = android.text.Html.fromHtml("<a href='https://console.cloud.google.com/apis/library/youtube.googleapis.com'>Bật API</a> · <a href='https://support.google.com/policies/answer/9664901'>Thông báo app trẻ em</a> · <a href='https://www.youtube.com/t/terms'>Điều khoản YouTube</a> · <a href='https://policies.google.com/privacy'>Riêng tư Google</a>", android.text.Html.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
        }
        label("Package: $packageName\nSHA-1 chứng thư APK đang cài: " + runCatching { YouTubeMetadata.signingSha1(this) }.getOrDefault("Chưa đọc được"))
        val key = field(if (settings.key().isBlank()) "API key" else "Đã lưu key · để trống để giữ", "").apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_NO
            imeOptions = android.view.inputmethod.EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        }
        val contact = field("Liên hệ người quản lý app / dữ liệu", settings.contact)
        val country = field("Mã quốc gia xem (VN); không lấy từ GPS", settings.country)
        label("Quyền riêng tư của DeX Kids — tính năng YouTube:\nApp dùng YouTube API Services. Khi bật, app gửi ID video và thông tin định danh ứng dụng để kiểm tra metadata; trình phát Google có thể nhận IP, thông tin thiết bị, dữ liệu phát, cookie/công nghệ tương tự, và hiển thị quảng cáo, liên kết, đề xuất. Chế độ youtube-nocookie không có nghĩa là không thu thập dữ liệu.\nApp không gửi tên/tuổi của trẻ, GPS, âm thanh hoặc câu trả lời tới YouTube. Key được mã hóa trên thiết bị. Danh sách link, duyệt nội dung và cấu hình nằm cục bộ; metadata không được lưu lâu dài. Không có đăng nhập Google, OAuth, upload, bình luận hay tạo playlist trên YouTube.\nPhụ huynh tự duyệt toàn bộ video. Nhãn Made for Kids không thay thế duyệt nội dung. Native YouTube có thể hiện nội dung ngoài danh sách duyệt. App không cam kết Premium/không quảng cáo. Video luôn hiển thị, không phát nền. Lỗi hoặc không được tự phát sẽ về nội dung offline.\nCó thể tắt YouTube ở thư viện; xóa cấu hình/key bên dưới và xóa link ở thư viện. Dữ liệu do Google xử lý tuân theo chính sách Google; các nút xóa này không xóa dữ liệu phía Google. Liên hệ quản lý dữ liệu theo thông tin bạn nhập ở trên.")
        val consent = CheckBox(this).apply {
            text = "Tôi dùng trong gia đình, tự duyệt từng video và đồng ý kết nối YouTube theo thông tin quyền riêng tư trên cùng Điều khoản YouTube."
            isChecked = settings.consented; column.addView(this)
        }
        button("Lưu cấu hình · dùng trình phát trong app") {
            try {
                settings.configure(key.text.toString().trim(), settings.notificationReference, contact.text.toString(),
                    country.text.toString().trim().uppercase(java.util.Locale.ROOT), consent.isChecked)
                settings.selected = true
                render()
            } catch (_: Exception) { AlertDialog.Builder(this).setMessage("Chưa lưu được. Kiểm tra key, mã quốc gia 2 chữ và độ dài các ô.").setPositiveButton("Đóng", null).show() }
        }
        button("Xóa key và cấu hình YouTube") {
            AlertDialog.Builder(this).setMessage("Xóa cấu hình phát nhúng trên thiết bị?")
                .setNegativeButton("Hủy", null).setPositiveButton("Xóa") { _, _ -> settings.delete(); render() }.show()
        }
        button("Quay lại") { finish() }
        setContentView(ScrollView(this).apply { addView(column) })
    }
}
