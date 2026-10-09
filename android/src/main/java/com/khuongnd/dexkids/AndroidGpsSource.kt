package com.khuongnd.dexkids

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import com.khuongnd.dexkids.geo.GeoFix

/**
 * Foreground-only opt-in adapter. It does not request permissions itself,
 * collect history, upload data, or start on launch.
 */
class AndroidGpsSource(private val context: Context) {
    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var listener: LocationListener? = null

    fun start(onFix: (GeoFix) -> Unit): Boolean {
        if (listener != null) return true
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return false
        val callback = LocationListener { location ->
            if (location.hasAccuracy()) {
                onFix(GeoFix(location.latitude, location.longitude, location.accuracy,
                    if (location.hasSpeed()) location.speed else 0f, location.time))
            }
        }
        return try {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 2f, callback)
            listener = callback
            true
        } catch (_: SecurityException) { false }
          catch (_: IllegalArgumentException) { false }
    }

    fun stop() {
        listener?.let { manager.removeUpdates(it) }
        listener = null
    }
}
