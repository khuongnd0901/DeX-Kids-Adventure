package com.khuongnd.dexkids.ai

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.khuongnd.dexkids.auth.ChatGptAuthManager
import com.khuongnd.dexkids.auth.ChatGptFailure

/** Parent-only entry; never opened from a child inference failure or background retry. */
class ChatGptSubscriptionActivity : Activity() {
    private lateinit var auth: ChatGptAuthManager
    private lateinit var settings: ChatGptSettings
    private lateinit var status: TextView
    private var worker: Thread? = null
    private var message = ""
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        auth = ChatGptAuthManager.get(this)
        settings = ChatGptSettings(this)
        render()
    }
    private fun render() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
        }
        status = TextView(this).apply { textSize = 20f }
        layout.addView(status)
        layout.addView(CheckBox(this).apply {
            text = "Cho phép Kids dùng ChatGPT Subscription cho AI (không gửi GPS/âm thanh)"
            isChecked = settings.enabled
            setOnCheckedChangeListener { _, checked ->
                settings.enabled = checked
                if (!checked) worker?.interrupt()
                updateStatus()
            }
        })
        fun button(label: String, action: () -> Unit) {
            layout.addView(Button(this).apply { text = label; setOnClickListener { action() } })
        }
        button("Continue with ChatGPT · Đăng nhập / kết nối lại") {
            AlertDialog.Builder(this).setTitle("Đăng nhập ChatGPT cho phụ huynh")
                .setMessage("Chỉ tiếp tục khi đã dừng chuyến chơi và xe đang dừng an toàn. " +
                    "Trình duyệt sẽ yêu cầu quyền dùng hạn mức gói ChatGPT. Không tự bật các quyền AI Kids.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Tiếp tục") { _, _ -> work {
                    auth.login { uri -> runOnUiThread {
                        runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                            .onFailure { auth.cancelLogin() }
                    } }
                    "Đã kết nối. Tải danh sách model để chọn."
                } }.show()
        }
        button("Tải danh sách model của tài khoản") { work {
            check(settings.enabled) { "CONSENT_REQUIRED" }
            val epoch = auth.sessionGeneration()
            val models = ChatGptModelRepository(auth).load()
            check(epoch == auth.sessionGeneration() && !Thread.currentThread().isInterrupted)
            val connection = auth.connectionKey() ?: throw ChatGptFailure("NOT_CONNECTED")
            settings.saveCatalog(connection, models)
            "Đã tải ${models.size} model. Chọn model mặc định."
        } }
        button("Chọn model AI mặc định") {
            val models = settings.models(auth.connectionKey())
            if (models.isEmpty()) { message = "Đăng nhập và tải danh sách model trước."; updateStatus() }
            else AlertDialog.Builder(this).setTitle("Model được cấp quyền")
                .setSingleChoiceItems(models.map { it.displayName }.toTypedArray(),
                    models.indexOfFirst { it.slug == settings.model() }) { dialog, index ->
                    worker?.interrupt()
                    settings.select(auth.connectionKey(), models[index].slug)
                    dialog.dismiss(); updateStatus()
                }.setNegativeButton("Đóng", null).show()
        }
        button("Test inference · câu tiếng Việt an toàn") { work {
            val text = KidsSubscriptionAiClient(this).request(
                "Reply with one short JSON object with key message, in Vietnamese.",
                "Hãy chào phụ huynh bằng một câu tiếng Việt ngắn.", null, 8192, consent = { settings.enabled })
            val value = org.json.JSONObject(text).getString("message")
            require(value.length in 1..250 && value.none { it.code < 32 })
            "Inference hoàn tất: $value"
        } }
        button("Hủy thao tác đang chạy") {
            worker?.interrupt(); auth.cancelLogin(); message = "Đã hủy."; updateStatus()
        }
        button("Logout · đăng xuất ChatGPT") {
            worker?.interrupt()
            work(replace = true) {
                settings.enabled = false
                if (auth.logout()) "Đã đăng xuất. Dữ liệu học và truyện vẫn được giữ."
                else "Đã xóa phiên trên máy. Chưa xác nhận thu hồi từ xa; kiểm tra quyền ứng dụng trong ChatGPT."
            }
        }
        button("Quay lại") { finish() }
        setContentView(ScrollView(this).apply { addView(layout) })
        updateStatus()
    }
    private fun updateStatus() {
        if (isDestroyed) return
        val state = when {
            auth.state == ChatGptAuthManager.State.SESSION_EXPIRED -> "Expired · đăng nhập lại"
            auth.state == ChatGptAuthManager.State.QUOTA_LIMITED -> "Hết hạn mức · chờ hoặc kiểm tra gói ChatGPT"
            auth.state == ChatGptAuthManager.State.AUTHENTICATING -> "Đang đăng nhập"
            auth.connected() -> "Connected"
            else -> "Disconnected"
        }
        status.text = "ChatGPT Subscription · $state\nModel: ${settings.model().ifBlank { "Chưa chọn" }}\n$message"
    }
    private fun work(replace: Boolean = false, action: () -> String) {
        if (!replace && worker?.isAlive == true) { message = "Thao tác trước chưa kết thúc."; updateStatus(); return }
        message = "Đang xử lý…"; updateStatus()
        worker = Thread({
            val result = try { action() } catch (e: Exception) {
                when ((e as? ChatGptFailure)?.code ?: e.message) {
                    "NOT_CONNECTED", "AI_DISABLED" -> "Disconnected · đăng nhập, bật quyền và chọn model trước."
                    "SESSION_EXPIRED", "SUBSCRIPTION_SESSION_EXPIRED" -> "Expired · đăng nhập lại."
                    "QUOTA_LIMITED", "SUBSCRIPTION_QUOTA_LIMITED" -> "Đã đạt hạn mức subscription. Không tự thử lại."
                    "ACCESS_DENIED", "SUBSCRIPTION_ACCESS_DENIED" -> "Tài khoản chưa cấp quyền dùng gói ChatGPT."
                    "CONSENT_REQUIRED" -> "Bật quyền dùng subscription trước."
                    else -> "Thao tác chưa hoàn tất. Kiểm tra kết nối/quyền hoặc thử kết nối lại."
                }
            }
            val me = Thread.currentThread()
            runOnUiThread { if (!isDestroyed && worker === me) { message = result; worker = null; updateStatus() } }
        }, "KidsSubscriptionSettings").apply { start() }
    }
    override fun onStop() {
        // The login browser must be allowed to return; other operations belong to visible parent UI.
        if (auth.state != ChatGptAuthManager.State.AUTHENTICATING) worker?.interrupt()
        super.onStop()
    }
    override fun onDestroy() { worker?.interrupt(); auth.cancelLogin(); super.onDestroy() }
}
