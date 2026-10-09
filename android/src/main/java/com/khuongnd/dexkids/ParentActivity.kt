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
        if (requestCode == 4003) {
            val granted = checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
            settings.allowChildMicrophone = granted && OnDeviceChildSpeech.available(this)
            permissionResultStatus = if (settings.allowChildMicrophone)
                "Microphone: phụ huynh đã cho phép nghe các câu trả lời ngắn."
            else "Chưa thể bật nghe offline. Cần quyền micro và dịch vụ nhận dạng trên thiết bị."
            render()
            return
        }
        if (requestCode == 4001) {
            val fineResult = grantResults.getOrNull(permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION))
            val coarseResult = grantResults.getOrNull(permissions.indexOf(Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionResultStatus = if (fineResult == PackageManager.PERMISSION_GRANTED)
                startGameOnCurrentDisplay(true, if (pendingLiveRoute) settings.selectedRouteId else null)
            else if (coarseResult == PackageManager.PERMISSION_GRANTED)
                "Precise location permission required; live journey not started."
            else "Location permission declined; no location data collected."
            pendingLiveRoute = false
            status.text = permissionResultStatus
        }
    }

    private fun render() {
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 40, 28, 32)
        }
        fun label(text: String) {
            column.addView(TextView(this).apply {
                this.text = text
                textSize = 17f
                setPadding(0, 8, 0, 12)
            })
        }
        fun button(title: String, action: () -> Unit) {
            column.addView(Button(this).apply {
                text = title
                setOnClickListener { action() }
            })
        }
        label("DeX Kids Adventure — single-display controls")
        status = TextView(this)
        column.addView(status)
        status.text = currentStatus()
        label("Use the mouse/keyboard on this DeX monitor. Phone touchscreen not required.")
        label("Age group: ${settings.ageGroup} • Session limit: ${settings.sessionMinutes} minutes")
        button("Age group 2–3") { settings.ageGroup = 3; render() }
        button("Age group 4–6") { settings.ageGroup = 5; render() }
        button("Limit 15 min") { settings.sessionMinutes = 15; render() }
        button("Limit 30 min") { settings.sessionMinutes = 30; render() }
        button("Limit 60 min") { settings.sessionMinutes = 60; render() }
        button(if (settings.audioOnly) "Audio-only: ON (minimal visual)" else "Audio-only: OFF (animated world)") {
            settings.audioOnly = !settings.audioOnly; render()
        }
        label("Audio-only reduces visuals; GPS/GPX, POI captions and timeout remain active. It is not a screen lock.")
        label("Để Capybara nói chuyện, bật giọng đọc tiếng Việt offline bên dưới. Không còn Quiet mode.")
        button(if (settings.allowOfflineSpeech) "Giọng đọc offline: BẬT" else "Bật giọng kể offline tiếng Việt") {
            settings.allowOfflineSpeech = !settings.allowOfflineSpeech; render()
        }
        label("Hội thoại hai chiều: chỉ nghe sau câu đố/câu hỏi, tối đa 9 giây/lượt. Không ghi âm, lưu nội dung hay gửi âm thanh lên server.")
        label(if (OnDeviceChildSpeech.available(this))
            "Có dịch vụ nhận dạng giọng nói trên thiết bị. Model tiếng Việt vẫn cần kiểm tra khi sử dụng."
            else "Thiết bị chưa có dịch vụ nhận dạng offline. App sẽ tiếp tục phụ đề/câu đố mà KHÔNG bật mic.")
        button(if (settings.allowChildMicrophone) "Tắt nghe bé (microphone)"
            else "Cho phép nghe câu trả lời của bé (offline)") {
            if (settings.allowChildMicrophone) {
                settings.allowChildMicrophone = false
                render()
            } else if (!OnDeviceChildSpeech.available(this)) {
                permissionResultStatus = "Không có dịch vụ ASR trên thiết bị. Không dùng nhận dạng online."
                render()
            } else if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                settings.allowChildMicrophone = true
                render()
            } else {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 4003)
            }
        }
        label("AI Kids: tạo câu đố từ dữ liệu địa danh đã có nguồn; có cache offline, không cần backend. API sử dụng Internet và có thể có quota hoặc tính phí.")
        val aiSettings = KidsAiSettings(this)
        button(if (aiSettings.enabled) "AI Kids: BẬT (thử nghiệm)" else "Bật AI Kids (mặc định TẮT)") {
            aiSettings.enabled = !aiSettings.enabled
            if (!aiSettings.enabled) aiSettings.cloudChildReply = false
            render()
        }
        button("Cấu hình Gemini / Groq · model · API key") { selectAiProvider() }
        button(if (aiSettings.cloudChildReply)
            "Tắt gửi câu trả lời của bé lên AI"
            else "Cho phép AI phản hồi từ lời bé (đồng ý riêng)") {
            if (aiSettings.cloudChildReply) {
                aiSettings.cloudChildReply = false
                render()
            } else {
                android.app.AlertDialog.Builder(this).setTitle("Xác nhận gửi nội dung lời bé")
                    .setMessage("Nếu bật, một phần CÂU TRẢ LỜI ĐƯỢC NHẬN DẠNG của bé " +
                        "có thể gửi tới Gemini/Groq khi đang online. Không gửi âm thanh hoặc GPS. " +
                        "Những câu có dấu hiệu thông tin cá nhân sẽ bị chặn, nhưng không đảm bảo lọc hết. " +
                        "Nhà cung cấp có thể lưu/xử lý dữ liệu theo điều khoản của họ. " +
                        "Tùy chọn này độc lập với quyền microphone và mặc định TẮT.")
                    .setNegativeButton("Không",null)
                    .setPositiveButton("Đồng ý bật") { _, _ ->
                        aiSettings.cloudChildReply = true; render()
                    }.show()
            }
        }
        button("Tạo cache 10–12 câu đố / địa danh cho 13 POI (cần Internet)") {
            prepareAiQuestionCache()
        }
        label("Cache được tạo trước chuyến đi. Nếu mất mạng/hết quota, app dùng câu đố offline. " +
            "Lưu ý: POI bản đồ và câu hỏi do AI soạn vẫn cần phụ huynh kiểm duyệt trước khi dùng với trẻ.")
        button("Reset local preferences") {
            android.app.AlertDialog.Builder(this)
                .setTitle("Reset local preferences?")
                .setMessage("Đặt lại tuổi, giới hạn thời gian, hình ảnh, quyền giọng kể và quyền nghe bé. Không lưu lịch sử GPS hoặc giọng nói.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset") { _, _ -> settings.resetLocalOptions(); render() }
                .show()
        }
        val selectedRouteId = settings.selectedRouteId
        label("LIVE GPS: Capybara nhận biết địa danh ở gần bằng dữ liệu OSM có nguồn, rồi kể chuyện và đố vui. Đây là ước tính gần vị trí xe, không phải chỉ đường.")
        label("Đang chọn: ${RouteKnowledgeCatalog.routeTitle(selectedRouteId)}")
        button("Chọn tuyến kiến thức") {
            val routes = RouteKnowledgeCatalog.ROUTES
            android.app.AlertDialog.Builder(this)
                .setTitle("Chọn hành trình khám phá")
                .setItems(routes.map { it.title() }.toTypedArray()) { _, index ->
                    settings.selectedRouteId = routes[index].id()
                    render()
                }.setNegativeButton("Đóng", null).show()
        }
        button("Xem 7 câu chuyện của tuyến (offline)") {
            runCatching {
                assets.open("routes/knowledge.tsv").use { RouteKnowledgeCatalog.parse(it) }
                    .cardsFor(settings.selectedRouteId, settings.ageGroup)
            }.onSuccess { cards ->
                val body = cards.mapIndexed { index, card ->
                    "${index + 1}. ${card.title()}\n${card.textVi()}\nNguồn: ${card.source()}"
                }.joinToString("\n\n")
                android.app.AlertDialog.Builder(this)
                    .setTitle("Khám phá: ${RouteKnowledgeCatalog.routeTitle(settings.selectedRouteId)}")
                    .setMessage("Đây là kiến thức giới thiệu, không xác nhận vị trí GPS.\n\n" + body)
                    .setPositiveButton("Đóng", null).show()
            }.onFailure {
                permissionResultStatus = "Dữ liệu kiến thức offline không hợp lệ."
                status.text = currentStatus()
            }
        }
        button("Start DEMO + 7 câu chuyện của tuyến") {
            startActivity(Intent(this, KidsActivity::class.java)
                .putExtra(KidsActivity.EXTRA_ROUTE_ID, settings.selectedRouteId))
        }
        button("Bắt đầu GPS THẬT · nhận diện địa danh · kể chuyện và đố vui") {
            permissionResultStatus = null
            pendingLiveRoute = true
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startGameOnCurrentDisplay(true, settings.selectedRouteId)
                pendingLiveRoute = false
            } else requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), 4001)
        }
        button("Start DEMO on this screen") {
            permissionResultStatus = null
            startGameOnCurrentDisplay(false)
        }
        label("Các vị trí POI chỉ được đối chiếu nguồn bản đồ, chưa khảo sát thực địa. App chỉ nói 'có thể ở gần', không khẳng định xe vừa đi qua.")
        button("Start HCMC sample journey (preview)") {
            startActivity(Intent(this, KidsActivity::class.java).apply {
                putExtra(KidsActivity.EXTRA_HCM_SAMPLE, true)
            })
        }
        button("Choose GPX file and start REPLAY") {
            permissionResultStatus = null
            // SAF picker returns a temporary read-only URI. No storage/media permission.
            val picker = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*" // Some GPX providers do not register application/gpx+xml.
            }
            @Suppress("DEPRECATION")
            startActivityForResult(picker, GPX_PICKER_REQUEST)
        }
        button("LIVE GPS toàn bộ POI offline (không chọn tuyến)") {
            pendingLiveRoute = false
            permissionResultStatus = null
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                startGameOnCurrentDisplay(true)
            else
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION), 4001)
        }
        button("Stop adventure") {
            permissionResultStatus = null
            KidsSessionControl.stop()
            status.text = "Stop requested"
        }
        label("Inside the adventure: click Parents menu once or press F10. No PIN or screen lock.")
        label("DeX mouse/keyboard and external-monitor launch are physical-device gates.")
        val scroll = ScrollView(this).apply { addView(column) }
        setContentView(scroll, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
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
        val age = this.settings.ageGroup
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
