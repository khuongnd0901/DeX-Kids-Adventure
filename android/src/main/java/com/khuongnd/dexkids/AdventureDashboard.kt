package com.khuongnd.dexkids

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.util.TypedValue
import com.khuongnd.dexkids.story.EntertainmentDirector

/**
 * M1: passive, FullHD single-display DeX HUD. Vietnamese is rendered natively.
 * Long AI questions use a wide, multiline auto-sized card instead of 2-line ellipsis.
 * The bus/active companions remain unobscured in the central and lower viewport.
 */
internal class AdventureDashboard(private val activity: Activity) {
    private val density = activity.resources.displayMetrics.density
    private val width = activity.resources.displayMetrics.widthPixels
    private val narrow = width < 900 * density
    private val headerWidth = minOf((width * (if (narrow) 0.48f else 0.32f)).toInt(), dp(400))
    private val questionWidth = minOf((width * (if (narrow) 0.85f else 0.43f)).toInt(), dp(510))
    private val modeLabel = panel(14f, Color.WHITE, 0xF0238A74.toInt(),2)
    private val topicLabel = panel(14f, 0xFF1C3B47.toInt(), 0xF3FFFFFF.toInt(),2)
    private val promptLabel = panel(19f, 0xFF1E3348.toInt(), 0xF7FFF8E2.toInt(),6).apply {
        setAutoSizeTextTypeUniformWithConfiguration(15,20,1,TypedValue.COMPLEX_UNIT_SP)
    }
    private val progressLabel = panel(13f, 0xFF1A4039.toInt(), 0xF3F7FFEE.toInt(),3)
    private var sauAge: Int = 5
    private var ongAge: Int = 4

    private fun dp(n: Int) = (n * density).toInt()

    private fun panel(size: Float, color: Int, backgroundColor: Int, lines: Int): TextView =
        TextView(activity).apply {
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(6), dp(12), dp(6))
            maxLines = lines
            // Never silently truncate an AI question; validation bounds all lesson lengths.
            ellipsize = null
            breakStrategy = android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY
            background = GradientDrawable().apply {
                cornerRadius = dp(11).toFloat()
                setColor(backgroundColor)
                setStroke(dp(1).coerceAtLeast(1), 0x80FFFFFF.toInt())
            }
            isClickable = false
            isFocusable = false
        }

    private fun attach(v: View, w: Int, gravity: Int, y: Int) {
        val params = FrameLayout.LayoutParams(w, ViewGroup.LayoutParams.WRAP_CONTENT, gravity)
        if (gravity and Gravity.BOTTOM == Gravity.BOTTOM)
            params.setMargins(dp(15), 0, 0, dp(y))
        else params.setMargins(dp(15), dp(y), 0, 0)
        activity.addContentView(v, params)
    }

    fun install(mode: String, demo: Boolean, sauAge: Int = 5, ongAge: Int = 4) {
        attach(modeLabel, headerWidth, Gravity.TOP or Gravity.START, 10)
        attach(topicLabel, headerWidth, Gravity.TOP or Gravity.START, 48)
        attach(promptLabel, questionWidth, Gravity.TOP or Gravity.START, 89)
        attach(progressLabel, minOf((width * .33f).toInt(), dp(410)),
            Gravity.BOTTOM or Gravity.START, 65)
        setMode(mode,sauAge,ongAge)
        topicLabel.text = "KHÁM PHÁ THẾ GIỚI"
        promptLabel.text = "Capybara đang chuẩn bị một câu chuyện mới..."
        progressLabel.text = if (demo)
            "HOẠT HÌNH MÔ PHỎNG · Cùng khám phá"
        else "GPS THẬT · Địa danh lân cận chỉ là ước tính"
    }

    fun setMode(mode: String, sauAge: Int = this.sauAge, ongAge: Int = this.ongAge) {
        require(sauAge in 4..10 && ongAge in 4..10)
        this.sauAge = sauAge
        this.ongAge = ongAge
        val actual = if (mode in setOf("SAU", "ONG", "BOTH")) mode else "BOTH"
        (modeLabel.background as GradientDrawable).setColor(when (actual) {
            "SAU" -> 0xF127A36E.toInt()
            "ONG" -> 0xF0D65A8D.toInt()
            else -> 0xF03AA79A.toInt()
        })
        modeLabel.text = when (actual) {
            "SAU" -> "SÂU · $sauAge TUỔI  •  CAPYBARA"
            "ONG" -> "ONG · $ongAge TUỔI  •  CAPYBARA"
            else -> "SÂU $sauAge · ONG $ongAge TUỔI  •  CAPYBARA"
        }
    }

    fun showBeat(beat: EntertainmentDirector.Beat, focus: EntertainmentDirector.Audience) {
        val child = when (focus) {
            EntertainmentDirector.Audience.SAU -> "Sâu khám phá"
            EntertainmentDirector.Audience.ONG -> "Ong khám phá"
            else -> "Hai anh em cùng chơi"
        }
        // AI random IDs do not encode genre. Use its validated, allowlisted visual topic.
        topicLabel.text = beat.visual().labelVi() + "  •  " + child
        promptLabel.text = beat.introduction()
        progressLabel.text = if (beat.englishWord().isNotEmpty())
            "TỪ TIẾNG ANH · ${beat.englishWord().uppercase()}"
        else "CÙNG QUAN SÁT · TRẢ LỜI · KHÁM PHÁ"
    }

    fun showConversation(label: String, text: String) {
        topicLabel.text = label.take(65)
        promptLabel.text = text // Let the bounded multiline card wrap, not truncate after 260 chars.
    }

    fun showPoiIntro(text: String, live: Boolean) {
        topicLabel.text = if (live) "KHÁM PHÁ GPS · ĐỊA DANH GẦN ĐÂY" else "GPX MÔ PHỎNG"
        promptLabel.text = text
        progressLabel.text = if (live)
            "© OpenStreetMap · chưa khớp tuyến thực tế"
        else "CHỈ MÔ PHỎNG · Không suy ra vị trí xe"
    }

    fun setVisualsEnabled(enabled: Boolean) {
        for (view in arrayOf(modeLabel,topicLabel,promptLabel,progressLabel))
            view.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    companion object {
        internal fun subjectFor(id: String): String = when {
            id == "care-sau-lips" -> "CHĂM SÓC BẢN THÂN"
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
