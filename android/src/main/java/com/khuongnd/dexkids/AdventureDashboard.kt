package com.khuongnd.dexkids

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.khuongnd.dexkids.story.EntertainmentDirector

/** Passive, single-display DeX HUD; native Android draws Vietnamese glyphs. */
internal class AdventureDashboard(private val activity: Activity) {
    private val density = activity.resources.displayMetrics.density
    private val width = activity.resources.displayMetrics.widthPixels
    private val narrow = width < 900 * density
    private val cardWidth = minOf((width * 0.30f).toInt(), px(280))
    private val modeLabel = panel(11f, Color.WHITE, 0xF0238A74.toInt())
    private val topicLabel = panel(11f, 0xFF1C3B47.toInt(), 0xF3FFFFFF.toInt())
    private val promptLabel = panel(12f, 0xFF1E3348.toInt(), 0xEDFFF8E2.toInt())
    private val progressLabel = panel(10f, 0xFF1A4039.toInt(), 0xF3F7FFEE.toInt())

    private fun px(n: Int) = (n * density).toInt()

    private fun panel(size: Float, color: Int, backgroundColor: Int): TextView =
        TextView(activity).apply {
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(px(8), px(4), px(8), px(4))
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            background = GradientDrawable().apply {
                cornerRadius = px(8).toFloat()
                setColor(backgroundColor)
                setStroke(px(2).coerceAtLeast(1), 0x80FFFFFF.toInt())
            }
            isClickable = false
            isFocusable = false
        }

    private fun attach(v: View, w: Int, gravity: Int, y: Int) {
        val params = FrameLayout.LayoutParams(w, ViewGroup.LayoutParams.WRAP_CONTENT, gravity)
        if (gravity and Gravity.BOTTOM == Gravity.BOTTOM)
            params.setMargins(px(15), 0, 0, px(y))
        else params.setMargins(px(15), px(y), 0, 0)
        activity.addContentView(v, params)
    }

    fun install(mode: String, demo: Boolean) {
        attach(modeLabel, cardWidth, Gravity.TOP or Gravity.START, 12)
        attach(topicLabel, cardWidth, Gravity.TOP or Gravity.START, 44)
        attach(promptLabel, cardWidth, Gravity.TOP or Gravity.START, 80)
        attach(progressLabel, minOf((width * 0.30f).toInt(), px(350)),
            Gravity.BOTTOM or Gravity.START, 65)
        setMode(mode)
        topicLabel.text = "KHÁM PHÁ THẾ GIỚI"
        promptLabel.text = "Capybara đang chuẩn bị một câu chuyện mới..."
        progressLabel.text = if (demo)
            "THẾ GIỚI MÔ PHỎNG  •  Khám phá  →  Đố vui  →  Kể chuyện"
        else "CHUYẾN ĐI THẬT  •  GPS gần vị trí, chưa khớp tuyến"
    }

    fun setMode(mode: String) {
        val actual = if (mode in setOf("SAU", "ONG", "BOTH")) mode else "BOTH"
        (modeLabel.background as GradientDrawable).setColor(when (actual) {
            "SAU" -> 0xF127A36E.toInt()
            "ONG" -> 0xF0D65A8D.toInt()
            else -> 0xF03AA79A.toInt()
        })
        modeLabel.text = when (actual) {
            "SAU" -> "SÂU (4 tuổi)  •  Capybara"
            "ONG" -> "ONG (3 tuổi)  •  Capybara"
            else -> "SÂU + ONG  •  Capybara"
        }
    }

    fun showBeat(beat: EntertainmentDirector.Beat, focus: EntertainmentDirector.Audience) {
        val child = when (focus) {
            EntertainmentDirector.Audience.SAU -> "Sâu khám phá"
            EntertainmentDirector.Audience.ONG -> "Ong khám phá"
            else -> "Hai anh em cùng chơi"
        }
        topicLabel.text = subjectFor(beat.id()) + "  •  " + child
        promptLabel.text = beat.introduction()
        progressLabel.text = "TRÒ CHƠI HOẠT HÌNH  •  Nhìn hình  →  Trả lời  →  Vỗ tay"
    }

    fun showConversation(label: String, text: String) {
        topicLabel.text = label.take(65)
        promptLabel.text = text.take(260)
    }

    fun showPoiIntro(text: String, live: Boolean) {
        topicLabel.text = if (live) "KHÁM PHÁ GPS  •  ĐỊA DANH LÂN CẬN" else "KHÁM PHÁ GPX MẪU"
        promptLabel.text = text.take(260)
        progressLabel.text = if (live)
            "© OpenStreetMap  •  Chỉ gần vị trí, chưa khớp tuyến"
        else "MÔ PHỎNG  •  Không suy ra vị trí thật"
    }

    fun setVisualsEnabled(enabled: Boolean) {
        for (view in arrayOf(modeLabel, topicLabel, promptLabel, progressLabel))
            view.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    companion object {
        internal fun subjectFor(id: String): String = when {
            listOf("bird", "cloud", "weather").any { id.contains(it) } -> "BẦU TRỜI"
            listOf("bus", "truck", "stop", "wheel", "round", "beep").any { id.contains(it) } -> "PHƯƠNG TIỆN"
            listOf("rabbit", "cat", "fox", "panda", "animal", "mouse").any { id.contains(it) } -> "ĐỘNG VẬT"
            listOf("count", "one", "shape").any { id.contains(it) } -> "ĐẾM SỐ & HÌNH"
            listOf("colors", "red", "green", "flower").any { id.contains(it) } -> "MÀU SẮC"
            listOf("tree", "river", "rain", "waves").any { id.contains(it) } -> "THIÊN NHIÊN"
            else -> "CÙNG CAPYBARA"
        }
    }
}
