package com.khuongnd.dexkids.media

import android.app.Activity
import android.graphics.*
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.*
import android.webkit.*
import android.widget.*
import org.json.JSONObject

/** One visible official IFrame, native sibling controls, no JavascriptInterface exposed to frames. */
class EmbeddedYouTubeHost(
    private val activity: Activity, private val video: YouTubeVideo,
    private val completed: (String) -> Unit, private val external: (Uri) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private val generation = SystemClock.elapsedRealtimeNanos()
    private val session = EmbeddedYouTubeSession(generation)
    private val nonce = java.util.UUID.randomUUID().toString()
    private val sourceDisplay = OfficialYouTube.externalDisplay(activity)
    private val displayManager = activity.getSystemService(android.hardware.display.DisplayManager::class.java)
    private val displayListener = object : android.hardware.display.DisplayManager.DisplayListener {
        override fun onDisplayAdded(id: Int) {}
        override fun onDisplayRemoved(id: Int) { if (id == sourceDisplay) finish("cancelled") }
        override fun onDisplayChanged(id: Int) {
            if (OfficialYouTube.externalDisplay(activity) != sourceDisplay) finish("cancelled")
        }
    }
    private val origin = "https://${activity.packageName}"
    private var web: WebView? = null
    private var port: WebMessagePort? = null
    private var popup: WebView? = null
    private var loadingSince = SystemClock.elapsedRealtime()
    private var disposed = false
    private val root = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL; setBackgroundColor(Color.rgb(16,28,37))
        isClickable = true; isFocusableInTouchMode = true
    }
    private val playerSpace = FrameLayout(activity)
    private val sidebar = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
    private val status = TextView(activity).apply { text = "Đang kiểm tra video…"; setTextColor(Color.WHITE); gravity = Gravity.CENTER }
    private var companion: CompanionView? = null
    private val watchdog = object : Runnable {
        override fun run() {
            if (disposed) return
            if (OfficialYouTube.externalDisplay(activity) != sourceDisplay || sourceDisplay == null || !OfficialYouTube.online(activity)) { finish("error"); return }
            if (session.state() in setOf(EmbeddedYouTubeSession.State.LOADING, EmbeddedYouTubeSession.State.BUFFERING) &&
                SystemClock.elapsedRealtime() - loadingSince > 45_000) { finish("error"); return }
            handler.postDelayed(this, 1000)
        }
    }
    fun start() {
        check(!disposed)
        root.addView(playerSpace, LinearLayout.LayoutParams(0, -1, 3f))
        root.addView(sidebar, LinearLayout.LayoutParams(0, -1, 1f))
        sidebar.addView(status, LinearLayout.LayoutParams(-1, -2))
        companion = CompanionView(activity).also { sidebar.addView(it, LinearLayout.LayoutParams(-1, 0, 1f)) }
        sidebar.addView(Button(activity).apply { text = "Tiếp tục hành trình"; isAllCaps = false; setOnClickListener { finish("cancelled") } })
        activity.addContentView(root, ViewGroup.LayoutParams(-1,-1))
        root.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (web != null && oldRight > oldLeft && (right-left != oldRight-oldLeft || bottom-top != oldBottom-oldTop)) finish("cancelled")
        }
        root.requestFocus()
        root.setOnKeyListener { _, code, event ->
            if (event.action == KeyEvent.ACTION_UP && code in setOf(KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_F10, KeyEvent.KEYCODE_MENU)) {
                finish("cancelled"); true
            } else false
        }
        displayManager.registerDisplayListener(displayListener, handler)
        handler.post(watchdog)
        val settings = YouTubeEmbedSettings(activity)
        if (!settings.selected || !settings.ready() || OfficialYouTube.externalDisplay(activity) == null) { finish("error"); return }
        val key = settings.key(); val country = settings.country
        Thread({
            val valid = runCatching { YouTubeMetadata.validate(activity.applicationContext, video, key, country) }.isSuccess
            handler.post {
                if (!disposed) {
                    if (valid && settings.ready() && settings.selected && root.isShown &&
                        OfficialYouTube.externalDisplay(activity) == sourceDisplay) loadPlayer()
                    else finish("error")
                }
            }
        }, "youtube-metadata").start()
    }
    private fun loadPlayer() {
        playerSpace.post {
            try {
            if (disposed) return@post
            val width = playerSpace.width
            val height = minOf(playerSpace.height, width * 9 / 16)
            val fittedWidth = minOf(width, height * 16 / 9)
            val density = activity.resources.displayMetrics.density
            if (fittedWidth / density < 200 || height / density < 200) { finish("error"); return@post }
            val view = WebView(activity)
            web = view
            view.settings.apply {
                javaScriptEnabled = true
                allowFileAccess = false; allowContentAccess = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                mediaPlaybackRequiresUserGesture = false // Parent opts into visible autoplay; still handle onAutoplayBlocked.
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(true)
            }
            CookieManager.getInstance().setAcceptThirdPartyCookies(view, false)
            view.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(v: WebView, request: WebResourceRequest): Boolean {
                    if (!request.isForMainFrame) return false
                    if (request.hasGesture()) openExternal(request.url) else finish("error")
                    return true
                }
                override fun onReceivedError(v: WebView, request: WebResourceRequest, error: WebResourceError) {
                    if (request.isForMainFrame) finish("error")
                }
                override fun onRenderProcessGone(v: WebView, detail: RenderProcessGoneDetail): Boolean { finish("error"); return true }
                override fun onPageFinished(v: WebView, url: String) {
                    if (disposed || port != null || Uri.parse(url).let { it.scheme != "https" || it.host != activity.packageName || (it.port != -1 && it.port != 443) }) return
                    val ports = v.createWebMessageChannel()
                    port = ports[0]
                    ports[0].setWebMessageCallback(object : WebMessagePort.WebMessageCallback() {
                        override fun onMessage(channel: WebMessagePort, message: WebMessage) {
                            val data = message.data ?: return
                            if (disposed || data.length > 256) return
                            runCatching {
                                val row = JSONObject(data)
                                if (!session.matchesGeneration(row.optString("generation", "")) || row.getString("videoId") != video.videoId()) return
                                val event = row.getString("event")
                                val old = session.state()
                                if (session.event(generation, event)) { dispose(); completed(event) }
                                else if (!session.finished()) {
                                    status.text = when (session.state()) {
                                        EmbeddedYouTubeSession.State.PLAYING -> "YouTube · ${video.title()}"
                                        EmbeddedYouTubeSession.State.PAUSED -> "Video đang tạm dừng"
                                        else -> "Đang tải video…"
                                    }
                                    if (old != session.state()) loadingSince = SystemClock.elapsedRealtime()
                                }
                            }.onFailure { finish("error") }
                        }
                    }, handler)
                    v.postWebMessage(WebMessage(nonce, arrayOf(ports[1])), Uri.parse(origin))
                }
            }
            view.webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) { request.deny() }
                override fun onCreateWindow(v: WebView, dialog: Boolean, gesture: Boolean, result: android.os.Message): Boolean {
                    if (!gesture || disposed) return false
                    popup?.destroy()
                    val target = WebView(activity)
                    popup = target
                    target.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(v: WebView, request: WebResourceRequest): Boolean {
                            openExternal(request.url); return true
                        }
                    }
                    (result.obj as WebView.WebViewTransport).webView = target
                    result.sendToTarget(); return true
                }
            }
            playerSpace.addView(view, FrameLayout.LayoutParams(fittedWidth, height, Gravity.CENTER))
            val html = activity.assets.open("youtube/player.html").bufferedReader().use { it.readText() }
                .replace("__GENERATION__", generation.toString()).replace("__VIDEO_ID__", video.videoId())
                .replace("__ORIGIN__", origin).replace("__NONCE__", nonce)
            loadingSince = SystemClock.elapsedRealtime()
            view.loadDataWithBaseURL("$origin/", html, "text/html", "UTF-8", null)
            } catch (_: Exception) { finish("error") }
        }
    }
    private fun openExternal(uri: Uri) {
        if (disposed) return
        if (uri.scheme != "https" || uri.host.isNullOrBlank()) { finish("error"); return }
        if (session.event(generation, "cancelled")) { dispose(); external(uri) }
    }
    fun finish(reason: String = "cancelled") {
        if (!disposed && session.event(generation, reason)) { dispose(); completed(reason) }
    }
    fun dispose() {
        if (disposed) return
        disposed = true
        handler.removeCallbacksAndMessages(null)
        runCatching { displayManager.unregisterDisplayListener(displayListener) }
        runCatching { port?.close() }; port = null
        runCatching { popup?.destroy() }; popup = null
        web?.let { view ->
            runCatching { (view.parent as? ViewGroup)?.removeView(view) }
            runCatching { view.stopLoading(); view.onPause() }
            runCatching { view.destroy() }
        }
        web = null
        runCatching { companion?.close() }; companion = null
        runCatching { (root.parent as? ViewGroup)?.removeView(root) }
    }
    private class CompanionView(activity: Activity) : View(activity) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val poses: List<Bitmap> = runCatching {
            val sheet = activity.assets.open("generated/t048/t048.png").use { BitmapFactory.decodeStream(it) }
            listOf(Bitmap.createBitmap(sheet, 4, 4, 224, 288), Bitmap.createBitmap(sheet,700,4,224,288)).also { sheet.recycle() }
        }.getOrDefault(emptyList())
        private var closed = false
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (closed || poses.isEmpty()) return
            val time = SystemClock.uptimeMillis()
            val bitmap = poses[((time / 3500) % poses.size).toInt()]
            val scale = minOf(width / 250f, height / 330f)
            val w = 224 * scale; val h = 288 * scale
            val x = (width-w)/2
            val y = (height-h)/2 + kotlin.math.sin(time / 600.0).toFloat() * 4 * scale
            canvas.drawBitmap(bitmap, null, RectF(x,y,x+w,y+h),paint)
            postInvalidateDelayed(50)
        }
        fun close() { closed = true; poses.forEach { it.recycle() } }
    }
}
