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

/** Parent-only phone controls; no game button is exposed to children. */
class ParentActivity : Activity() {
    private lateinit var settings: ParentSettings
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = ParentSettings(this)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized)
            status.text = if (KidsSessionControl.active()) "Adventure session active" else "Session stopped"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 4001) {
            status.text = if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED)
                DisplayRouter(this).launchOnExternalDisplay(true)
            else "Location permission declined; no location data collected."
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
        label("DeX Kids Adventure — Parent controls")
        status = TextView(this)
        column.addView(status)
        status.text = if (KidsSessionControl.active()) "Adventure session active" else "Session stopped"
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
        button("Parent preview on this phone (EXPLICIT)") {
            startActivity(Intent(this, KidsActivity::class.java))
        }
        button("Start on external DeX display (no fallback)") {
            status.text = DisplayRouter(this).launchOnExternalDisplay()
        }
        button("Start LIVE GPS on external DeX display (permission required)") {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                status.text = DisplayRouter(this).launchOnExternalDisplay(true)
            else
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 4001)
        }
        button("Stop adventure") {
            KidsSessionControl.stop()
            status.text = "Stop requested"
        }
        label("DeX routing and secure IPC are hardware-gated; existing Assistant is untouched.")
        val scroll = ScrollView(this).apply { addView(column) }
        setContentView(scroll, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }
}
