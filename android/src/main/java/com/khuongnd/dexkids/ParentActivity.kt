package com.khuongnd.dexkids

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.EditText
import android.widget.CheckBox
import android.text.InputType
import com.khuongnd.dexkids.ai.KidsAiProvider
import com.khuongnd.dexkids.ai.KidsAiSettings
import com.khuongnd.dexkids.ai.KidsAiKeys
import com.khuongnd.dexkids.ai.KidsAiQuizCache
import com.khuongnd.dexkids.ai.KidsAiGateway
import com.khuongnd.dexkids.ai.KidsQuiz
import com.khuongnd.dexkids.story.OfflineNarrationCatalog
import com.khuongnd.dexkids.story.PoiDialogueCatalog
import com.khuongnd.dexkids.story.RouteKnowledgeCatalog

/** Parent dashboard on the SAME DeX display as the game, never on a separate phone screen. */
class ParentActivity : Activity() {
    private lateinit var settings: ParentSettings
    private lateinit var status: TextView
    private var permissionResultStatus: String? = null
    private val GPX_PICKER_REQUEST = 4002
    private var pendingLiveRoute = false
    private var aiWorker: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = ParentSettings(this)
        pendingLiveRoute = savedInstanceState?.getBoolean("pending_live_route", false) ?: false
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized)
            status.text = currentStatus()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            4001 -> {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) requestMicrophoneThenStart()
                else {
                    pendingLiveRoute = false
                    permissionResultStatus = "Cần quyền vị trí chính xác để nhận biết địa danh. Có thể xem DEMO."
                    render()
                }
            }
            4003 -> {
                if (pendingLiveRoute) {
                    // A denied optional microphone never blocks GPS or the offline story.
                    val voiceGranted = checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
                    permissionResultStatus = if (voiceGranted) "Đã cấp micro trên thiết bị."
                        else "Chưa cấp micro; kể chuyện và phụ đề vẫn hoạt động."
                    finishLiveStart()
                } else {
                    permissionResultStatus = if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED) "Đã cấp micro."
                        else "Không có quyền micro; chỉ sử dụng phụ đề và câu đố offline."
                    render()
                }
            }
        }
    }

    private fun beginLive() {
        pendingLiveRoute = true
        permissionResultStatus = null
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), 4001)
        else requestMicrophoneThenStart()
    }

    private fun requestMicrophoneThenStart() {
        if (settings.allowChildMicrophone && OnDeviceChildSpeech.available(this) &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 4003)
        } else finishLiveStart()
    }

    private fun finishLiveStart() {
        if (!pendingLiveRoute) return
        pendingLiveRoute = false
        permissionResultStatus = startGameOnCurrentDisplay(true)
    }

    /** A compact home screen; rarely used controls are inside one Settings dialog. */
    private fun render() {
        val dp = resources.displayMetrics.density
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((24*dp).toInt(),(24*dp).toInt(),
                (24*dp).toInt(),(24*dp).toInt())
        }
        fun label(value: String, size: Float = 16f) {
            column.addView(TextView(this).apply {
                text = value
                textSize = size
                setPadding(0,(7*dp).toInt(),0,(10*dp).toInt())
            })
        }
        fun primary(title: String, action: () -> Unit) {
            column.addView(Button(this).apply {
                text = title
                isAllCaps = false
                textSize = 18f
                setOnClickListener { action() }
            })
        }
        label("DeX Kids Adventure", 25f)
        status = TextView(this)
        column.addView(status)
        status.text = currentStatus()
        val gpsReady = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val micReady = checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED && OnDeviceChildSpeech.available(this)
        label("Người xem: " + settings.audienceLabel + " · " + settings.sessionMinutes +
            " phút · GPS " + (if (gpsReady) "✓" else "cần cấp quyền") +
            " · Micro " + (if (micReady) "✓" else "chưa sẵn sàng"), 14f)
        column.addView(Button(this).apply {
            text = "Chọn người xem · " + settings.audienceLabel
            isAllCaps = false
            textSize = 18f
            setOnClickListener { showAudiencePicker() }
        })
        primary("BẮT ĐẦU · GPS thật và Capybara trò chuyện") { beginLive() }
        primary("XEM THỬ · hoạt hình DEMO") {
            startActivity(Intent(this, KidsActivity::class.java))
        }
        primary("Cài đặt · người xem, thời gian, giọng nói, AI") { showSetupMenu() }
        primary("Dừng hành trình") {
            KidsSessionControl.stop()
            status.text = "Đã gửi yêu cầu dừng"
        }
        label("Mặc định: giọng kể offline, hỏi đáp bằng micro và AI Kids được bật. " +
            "Android vẫn cần bạn cấp quyền vị trí/micro lần đầu; AI cần cấu hình key. " +
            "Không gửi lời bé lên AI trừ khi phụ huynh cho phép riêng.", 14f)
        label("Chỉ một màn hình DeX · điều khiển bằng chuột/F10 · POI gần xe là ước tính, không phải chỉ đường.",14f)
        setContentView(ScrollView(this).apply { addView(column) },
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun showAudiencePicker() {
        val ids = arrayOf("SAU", "ONG", "BOTH")
        val labels = arrayOf("Cho Sâu (3 tuổi)", "Cho Ong (2 tuổi)",
            "Cả Sâu và Ong cùng xem (2–3 tuổi)")
        val selected = ids.indexOf(settings.audienceMode).coerceAtLeast(0)
        android.app.AlertDialog.Builder(this).setTitle("Chọn người xem")
            .setSingleChoiceItems(labels, selected) { dialog, which ->
                settings.audienceMode = ids[which]
                dialog.dismiss()
                render()
            }.setNegativeButton("Hủy", null).show()
    }

    private fun showSetupMenu() {
        val items = arrayOf(
            "Người xem: " + settings.audienceLabel,
            "Thời lượng: " + settings.sessionMinutes + " phút",
            "Giọng kể và microphone",
            "AI Kids · Gemini/Groq",
            "Chọn tuyến kiến thức",
            "Công cụ mở rộng · GPX / nội dung mẫu",
            "Khôi phục cấu hình mặc định"
        )
        android.app.AlertDialog.Builder(this).setTitle("Cài đặt phụ huynh")
            .setItems(items) { _, i ->
                when(i) {
                    0 -> showAudiencePicker()
                    1 -> android.app.AlertDialog.Builder(this).setTitle("Thời gian mỗi chuyến")
                        .setItems(arrayOf("15 phút","30 phút","60 phút")) { _,index ->
                            settings.sessionMinutes = listOf(15,30,60)[index]; render()
                        }.show()
                    2 -> showVoiceSettings()
                    3 -> showAiSettings()
                    4 -> chooseRoute()
                    5 -> showAdvancedTools()
                    6 -> android.app.AlertDialog.Builder(this)
                        .setTitle("Khôi phục thiết lập?")
                        .setMessage("Bật lại các tính năng offline và mặc định AI; không tự cấp quyền Android, không đổi API key hoặc quyền chia sẻ lời trẻ.")
                        .setNegativeButton("Hủy",null)
                        .setPositiveButton("Khôi phục") { _, _ ->
                            settings.resetLocalOptions()
                            // Reset only non-sensitive AI preference, never child cloud sharing.
                            KidsAiSettings(this).enabled = true
                            render()
                        }.show()
                }
            }.setNegativeButton("Đóng",null).show()
    }

    private fun showVoiceSettings() {
        val choices = arrayOf(
            if (settings.allowOfflineSpeech) "Tắt giọng kể offline" else "Bật giọng kể offline",
            if (settings.allowChildMicrophone) "Tắt nghe câu trả lời (micro)"
                else "Bật nghe câu trả lời (micro)",
            if (settings.audioOnly) "Bật hình ảnh hoạt hình" else "Chỉ âm thanh (giảm đồ họa)",
            "Xin quyền micro khi cần"
        )
        android.app.AlertDialog.Builder(this).setTitle("Âm thanh và microphone")
            .setItems(choices) { _, i ->
                when(i) {
                    0 -> settings.allowOfflineSpeech = !settings.allowOfflineSpeech
                    1 -> {
                        if (settings.allowChildMicrophone) settings.allowChildMicrophone = false
                        else settings.allowChildMicrophone = true
                    }
                    2 -> settings.audioOnly = !settings.audioOnly
                    3 -> {
                        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
                            PackageManager.PERMISSION_GRANTED &&
                            OnDeviceChildSpeech.available(this))
                            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 4003)
                        else {
                            permissionResultStatus = if (OnDeviceChildSpeech.available(this))
                                "Micro đã sẵn sàng." else "Máy chưa có ASR offline."
                        }
                    }
                }
                render()
            }.setNegativeButton("Đóng",null).show()
    }

    private fun showAiSettings() {
        val aiSettings = KidsAiSettings(this)
        val choices = arrayOf(
            if (aiSettings.enabled) "Tắt AI Kids" else "Bật AI Kids",
            "Cấu hình Gemini / Groq · model · API key",
            "Tạo cache 10–12 câu đố / địa danh",
            if (aiSettings.cloudChildReply) "Tắt gửi câu trả lời của bé lên AI"
                else "Cho phép AI phản hồi từ lời bé (đồng ý riêng)"
        )
        android.app.AlertDialog.Builder(this).setTitle("AI Kids · tạo câu hỏi offline")
            .setMessage("AI tạo câu hỏi từ nội dung POI có nguồn. Không cần backend. " +
                "Mọi API/model cần key và xác minh quota/chi phí.")
            .setItems(choices) { _,i ->
                when(i) {
                    0 -> {
                        aiSettings.enabled = !aiSettings.enabled
                        if (!aiSettings.enabled) aiSettings.cloudChildReply = false
                        render()
                    }
                    1 -> selectAiProvider()
                    2 -> prepareAiQuestionCache()
                    3 -> {
                        if (aiSettings.cloudChildReply) {
                            aiSettings.cloudChildReply = false
                            render()
                        } else android.app.AlertDialog.Builder(this)
                            .setTitle("Cho phép gửi văn bản lời bé?")
                            .setMessage("Chỉ văn bản trả lời được nhận dạng, không phải âm thanh hoặc GPS, " +
                                "có thể gửi tới Gemini/Groq. Bộ lọc thông tin cá nhân không hoàn hảo. " +
                                "Nhà cung cấp xử lý dữ liệu theo điều khoản riêng. Quyền này mặc định TẮT.")
                            .setNegativeButton("Không",null)
                            .setPositiveButton("Tôi đồng ý") { _,_ ->
                                aiSettings.cloudChildReply = true
                                render()
                            }.show()
                    }
                }
            }.setNegativeButton("Đóng",null).show()
    }

    private fun chooseRoute() {
        val routes = RouteKnowledgeCatalog.ROUTES
        android.app.AlertDialog.Builder(this).setTitle("Chọn chủ đề chuyến đi")
            .setItems(routes.map { it.title() }.toTypedArray()) { _,i ->
                settings.selectedRouteId = routes[i].id()
                render()
            }.show()
    }

    private fun showAdvancedTools() {
        android.app.AlertDialog.Builder(this).setTitle("Công cụ mở rộng")
            .setItems(arrayOf(
                "DEMO · 7 câu chuyện theo tuyến",
                "Xem nội dung kiến thức tuyến đã chọn",
                "GPX REPLAY · chọn file",
                "Xem mẫu HCMC (mô phỏng)"
            )) { _,i ->
                when(i) {
                    0 -> startActivity(Intent(this, KidsActivity::class.java)
                        .putExtra(KidsActivity.EXTRA_ROUTE_ID,settings.selectedRouteId))
                    1 -> {
                        runCatching {
                            assets.open("routes/knowledge.tsv").use { RouteKnowledgeCatalog.parse(it) }
                                .cardsFor(settings.selectedRouteId,settings.activeAge)
                        }.onSuccess { cards ->
                            android.app.AlertDialog.Builder(this)
                                .setTitle(RouteKnowledgeCatalog.routeTitle(settings.selectedRouteId))
                                .setMessage(cards.joinToString("\n\n") { it.title() + ": " + it.textVi() })
                                .setPositiveButton("Đóng",null).show()
                        }.onFailure { permissionResultStatus = "Không đọc được kiến thức offline";render() }
                    }
                    2 -> {
                        val picker = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                        }
                        @Suppress("DEPRECATION")
                        startActivityForResult(picker,GPX_PICKER_REQUEST)
                    }
                    3 -> startActivity(Intent(this,KidsActivity::class.java)
                        .putExtra(KidsActivity.EXTRA_HCM_SAMPLE,true))
                }
            }.show()
    }


    private fun selectAiProvider() {
        android.app.AlertDialog.Builder(this).setTitle("AI provider (trực tiếp từ Android)")
            .setItems(KidsAiProvider.entries.map { it.title }.toTypedArray()) { _, i ->
                editAiProvider(KidsAiProvider.entries[i])
            }.setNegativeButton("Đóng",null).show()
    }

    private fun editAiProvider(p: KidsAiProvider) {
        val settings = KidsAiSettings(this)
        val keys = KidsAiKeys(this)
        val model = EditText(this).apply {
            hint = "Model ID, ví dụ gemini-... / llama-..."
            setSingleLine(true)
            setText(settings.model(p))
        }
        val secret = EditText(this).apply {
            hint = if (keys.configured(p)) "Key đã lưu · để trống nếu giữ nguyên"
                else "Nhập API key (chỉ lưu trên máy)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine(true)
        }
        val active = CheckBox(this).apply {
            text = "Bật provider " + p.title
            isChecked = settings.active(p)
        }
        val free = CheckBox(this).apply {
            text = "Tôi đã tự xác minh model thuộc Free Tier/quota phù hợp"
            isChecked = settings.freeTierAcknowledged(p)
        }
        val preferred = CheckBox(this).apply {
            text = "Đặt làm provider ưu tiên"
            isChecked = settings.provider == p
        }
        val view = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(25,18,25,12)
            addView(model); addView(secret); addView(active); addView(free); addView(preferred)
        }
        android.app.AlertDialog.Builder(this).setTitle("AI Kids · " + p.title)
            .setView(view).setNegativeButton("Hủy",null)
            .setNeutralButton("Xóa key") { _, _ ->
                keys.delete(p)
                settings.setActive(p,false)
                permissionResultStatus = "Đã xóa API key " + p.title
                render()
            }
            .setPositiveButton("Lưu") { _, _ ->
                runCatching {
                    val chosen = model.text.toString().trim()
                    val modelChanged = chosen.isNotEmpty() && chosen != settings.model(p)
                    if (modelChanged) settings.setModel(p,chosen)
                    if (secret.text.isNotBlank()) keys.save(p,secret.text.toString())
                    settings.setActive(p,active.isChecked)
                    // A previously checked Free Tier box cannot validate a NEW model.
                    settings.setFreeTierAcknowledged(p,!modelChanged && free.isChecked)
                    if (preferred.isChecked) settings.provider = p
                }.onSuccess {
                    permissionResultStatus = "Đã lưu cấu hình " + p.title +
                        ". Key " + if (keys.configured(p)) "đã thiết lập." else "chưa có."
                }.onFailure { permissionResultStatus = "Cấu hình AI không hợp lệ, kiểm tra model/key" }
                render()
            }.show()
    }

    private fun prepareAiQuestionCache() {
        if (aiWorker?.isAlive == true) {
            permissionResultStatus = "Đang tạo cache AI, không chạy đồng thời."
            render(); return
        }
        val settings = KidsAiSettings(this)
        val gateway = KidsAiGateway(this)
        if (!settings.enabled || !gateway.ready()) {
            permissionResultStatus = "Bật AI Kids, cấu hình provider/model/key và xác nhận Free Tier trước."
            render(); return
        }
        permissionResultStatus = "Đang tạo cache câu đố, chỉ gửi văn bản POI có nguồn."
        render()
        val cache = KidsAiQuizCache(this)
        val age = this.settings.activeAge
        aiWorker = Thread({
            var saved = 0
            var failed = 0
            try {
                val narrations = assets.open("narration/live-landmarks.tsv").use {
                    OfflineNarrationCatalog.parse(it)
                }
                val dialogue = assets.open("poi/live-dialogue.tsv").use {
                    PoiDialogueCatalog.parse(it)
                }
                val ids = assets.open("poi/live-landmarks.tsv").bufferedReader().use { input ->
                    input.lineSequence().drop(2).filter { it.isNotBlank() }
                        .map { it.substringBefore('\t') }.take(50).toList()
                }
                for (id in ids) {
                    if (Thread.currentThread().isInterrupted) break
                    val cue = narrations.findByPoiId(id).orElse(null) ?: continue
                    val card = dialogue.find(id).orElse(null) ?: continue
                    val base = KidsQuiz(card.quiz(),card.answer(),card.chat())
                    if (cache.get(id,age,cue.textVi(),base).isNotEmpty()) {
                        saved++; continue
                    }
                    try {
                        val questions = gateway.generate(id,cue.textVi(),base,age)
                        if (Thread.currentThread().isInterrupted) break
                        cache.put(id,age,cue.textVi(),base,questions)
                        saved++
                    } catch (_: Exception) { failed++ }
                    val progress = "Cache AI: " + saved + "/" + ids.size +
                        " POI có cache, lỗi hoặc hết quota: " + failed
                    runOnUiThread {
                        if (!isFinishing && !isDestroyed && ::status.isInitialized)
                            status.text = progress
                    }
                }
            } catch (_: Exception) { failed++ }
            val result = "Cache offline: " + saved +
                " địa danh có câu đố; lỗi/hết quota: " + failed +
                ". AI có thể cần model hỗ trợ JSON."
            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    permissionResultStatus = result
                    if (::status.isInitialized) status.text = result
                }
            }
        }, "dexkids-ai-precache").also { it.start() }
    }

    override fun onDestroy() {
        aiWorker?.interrupt()
        super.onDestroy()
    }

    @Deprecated("Activity result bridge retained because ParentActivity extends platform Activity")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != GPX_PICKER_REQUEST) return
        val uri = if (resultCode == RESULT_OK) data?.data else null
        if (uri == null || uri.scheme != "content") {
            permissionResultStatus = "No local GPX document selected."
            status.text = currentStatus()
            return
        }
        // The document permission is transient. Do not persist grants or GPS history.
        startActivity(Intent(this, KidsActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            this.data = uri
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(KidsActivity.EXTRA_GPX_REPLAY, true)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("pending_live_route", pendingLiveRoute)
        super.onSaveInstanceState(outState)
    }

    private fun startGameOnCurrentDisplay(liveGps: Boolean, routeId: String? = null): String {
        // Inherits this dashboard's DeX display. Does not imply road matching.
        val target = Intent(this, KidsActivity::class.java)
            .putExtra(KidsActivity.EXTRA_LIVE_GPS, liveGps)
        if (routeId != null && RouteKnowledgeCatalog.isSupportedRoute(routeId))
            target.putExtra(KidsActivity.EXTRA_ROUTE_ID, routeId)
        startActivity(target)
        return "Adventure started on this display."
    }

    private fun currentStatus(): String = permissionResultStatus
        ?: if (KidsSessionControl.active()) "Adventure session active" else "Session stopped"
}
