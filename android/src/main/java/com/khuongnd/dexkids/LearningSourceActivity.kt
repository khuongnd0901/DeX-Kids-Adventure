package com.khuongnd.dexkids

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*

/** Mouse/keyboard parent controls on the current DeX display. */
class LearningSourceActivity : Activity() {
    private lateinit var status: TextView
    private var busy = false
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (24 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }
        fun button(label: String, action: () -> Unit) {
            column.addView(Button(this).apply { text = label; setOnClickListener { if (!busy) action() } })
        }
        column.addView(TextView(this).apply { text = "Câu hỏi và tiếng Anh"; textSize = 26f })
        column.addView(TextView(this).apply {
            text = "Nhập file TSV UTF-8 hoặc dùng URL HTTPS Raw tới file TSV. File mới thay toàn bộ bộ câu hỏi/tiếng Anh; khi lỗi hoặc mất mạng giữ bộ đã lưu. Áp dụng ở hành trình mới. Không gửi câu trả lời hay dữ liệu của bé."
            textSize = 18f
        })
        column.addView(CheckBox(this).apply {
            text = "Kể chuyện sau mỗi 5 câu hỏi"
            isChecked = getSharedPreferences("learning_source_v1", 0).getBoolean("interleave", true)
            setOnCheckedChangeListener { _, checked ->
                getSharedPreferences("learning_source_v1", 0).edit().putBoolean("interleave", checked).apply()
            }
        })
        status = TextView(this).apply { textSize = 18f }; column.addView(status)
        button("Chọn file TSV") {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"; addCategory(Intent.CATEGORY_OPENABLE)
            }, 681)
        }
        button("Nguồn HTTPS · tự cập nhật khi mở ứng dụng") {
            val field = EditText(this).apply {
                setSingleLine(true); hint = "https://…/learning.tsv"; setText(LearningSource.url(this@LearningSourceActivity))
                inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI
            }
            val dialog = AlertDialog.Builder(this).setTitle("Nguồn câu hỏi HTTPS")
                .setView(field).setNegativeButton("Hủy", null).setPositiveButton("Lưu và tải", null).create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    try {
                        LearningSource.configure(this, field.text.toString())
                        dialog.dismiss(); refresh(); download()
                    } catch (_: IllegalArgumentException) { field.error = "Cần URL HTTPS hợp lệ, không kèm tài khoản." }
                }
            }
            dialog.show()
        }
        button("Tải lại nguồn HTTPS") { download() }
        button("Dùng lại bộ có sẵn") { LearningSource.useBuiltin(this); refresh() }
        button("Quay lại") { finish() }
        setContentView(ScrollView(this).apply { addView(column) })
        refresh()
    }
    private fun refresh() {
        val label = when (LearningSource.mode(this)) {
            "file" -> "File đã nhập"
            "https" -> "HTTPS tự cập nhật"
            else -> "Bộ có sẵn"
        }
        if (!isDestroyed) status.text = "Nguồn: $label\n${LearningSource.status(this)}"
    }
    private fun download() {
        if (LearningSource.mode(this) != "https") { status.text = "Hãy lưu URL HTTPS trước."; return }
        busy = true; status.text = "Đang tải…"
        LearningSource.request(this, true) { runOnUiThread { busy = false; refresh() } }
    }
    @Deprecated("Framework document picker")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 681 || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        busy = true; status.text = "Đang kiểm tra file…"
        val app = applicationContext
        Thread({
            val success = runCatching {
                app.contentResolver.openInputStream(uri).use {
                    requireNotNull(it); LearningSource.importFile(app, it)
                }
            }.isSuccess
            runOnUiThread {
                busy = false
                if (success) refresh() else status.text = "File không hợp lệ hoặc không đọc được. Bộ cũ được giữ nguyên."
            }
        }, "dexkids-learning-import").apply { isDaemon = true; start() }
    }
}
