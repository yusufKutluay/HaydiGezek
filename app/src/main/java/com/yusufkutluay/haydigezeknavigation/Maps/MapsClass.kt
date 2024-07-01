package com.yusufkutluay.haydigezeknavigation.Maps

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MapsClass(private val context: Context) {

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    fun getCurrentCityName(onCityNameReceived: (String) -> Unit) {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val cityName = getCityName(location.latitude, location.longitude)
                onCityNameReceived(cityName)
                locationManager.removeUpdates(this)
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
        }
    }

    private fun getCityName(lat: Double, lon: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address> = geocoder.getFromLocation(lat, lon, 1)!!
        return if (addresses.isNotEmpty()) {
            addresses[0].locality ?: "Şehir bulunamadı"
        } else {
            "Şehir bulunamadı"
        }
    }
}
