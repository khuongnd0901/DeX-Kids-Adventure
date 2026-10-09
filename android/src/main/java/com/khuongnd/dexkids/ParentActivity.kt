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

/** Parent dashboard on the SAME DeX display as the game, never on a separate phone screen. */
class ParentActivity : Activity() {
    private lateinit var settings: ParentSettings
    private lateinit var status: TextView
    private var permissionResultStatus: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = ParentSettings(this)
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
        if (requestCode == 4001) {
            val fineResult = grantResults.getOrNull(permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION))
            val coarseResult = grantResults.getOrNull(permissions.indexOf(Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionResultStatus = if (fineResult == PackageManager.PERMISSION_GRANTED)
                startGameOnCurrentDisplay(true)
            else if (coarseResult == PackageManager.PERMISSION_GRANTED)
                "Precise location permission required; live journey not started."
            else "Location permission declined; no location data collected."
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
        button(if (settings.quiet) "Quiet mode: ON" else "Quiet mode: OFF") {
            settings.quiet = !settings.quiet; render()
        }
        label("Audio is OFF by default. Offline speech requires explicit parent approval and an installed offline Vietnamese voice.")
        button(if (settings.allowOfflineSpeech) "Disable offline speech" else "Allow offline speech") {
            settings.allowOfflineSpeech = !settings.allowOfflineSpeech; render()
        }
        button("Start DEMO on this screen") {
            permissionResultStatus = null
            startGameOnCurrentDisplay(false)
        }
        button("Start LIVE GPS on this screen") {
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
        label("Inside the adventure: hold Parents or press F10 to manage the session on this monitor.")
        label("DeX mouse/keyboard and external-monitor launch are physical-device gates.")
        val scroll = ScrollView(this).apply { addView(column) }
        setContentView(scroll, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun startGameOnCurrentDisplay(liveGps: Boolean): String {
        // Starting from this Activity inherits its display. No cross-display routing.
        startActivity(Intent(this, KidsActivity::class.java).putExtra(KidsActivity.EXTRA_LIVE_GPS, liveGps))
        return "Adventure started on this display."
    }

    private fun currentStatus(): String = permissionResultStatus
        ?: if (KidsSessionControl.active()) "Adventure session active" else "Session stopped"
}
