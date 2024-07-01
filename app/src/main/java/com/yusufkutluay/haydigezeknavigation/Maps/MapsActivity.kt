package com.yusufkutluay.haydigezeknavigation.Maps

import android.Manifest
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import com.yusufkutluay.haydigezeknavigation.AlertDiaolog.Uyari
import com.yusufkutluay.haydigezeknavigation.Firebase.FirestoreDatabase
import com.yusufkutluay.haydigezeknavigation.Model.RotaListModel
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.databinding.ActivityMapsBinding
import io.opencensus.stats.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val db = FirestoreDatabase()
    private lateinit var database: FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private var listKonum = ArrayList<RotaListModel>()
    private var selectedLocations = ArrayList<LatLng>()
    private var totalDistance = 0f
    private var totalDuration = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.progressBar7.visibility = android.view.View.VISIBLE
        binding.listeGoster.isEnabled = false

        val uyari = Uyari()
        uyari.kontrolEtEthernet(this)

        //alertdialog ile uyarı yapıldı ve konum kapalıysa etkinleştirme ekranı açıldı
        if (!isLocationEnabled()) {
            val uyariYap = AlertDialog.Builder(this)
            uyariYap.setTitle("Uyarı")
            uyariYap.setMessage("Lütfen konum ayarlarınızı kontrol ediniz")
            uyariYap.setPositiveButton("Etkinleştir") { dialog, which ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

            uyariYap.setNegativeButton("İptal") { dialog, which ->
                dialog.dismiss()
            }
            uyariYap.setCancelable(false)
            uyariYap.create().show()
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val myKonum = LatLng(location.latitude, location.longitude)

                    database.collection("usersRota")
                        .document(auth.currentUser?.email!!)
                        .collection("selectedPlaces")
                        .get()
                        .addOnSuccessListener {
                            if (it.isEmpty){
                                binding.textView8.text = "Toplam Mesafe : "
                                binding.textView9.text = "Toplam Süre : "
                            }
                        }
                    binding.listeGoster.isEnabled = true

                    binding.listeGoster.setOnClickListener {
                        db.getRota { rotaList ->

                            // Konuma göre en yakından en uzağa sırala
                            rotaList.sortBy { calculateDistance(myKonum, LatLng(it.enlemRota, it.boylamRota)) }

                            // Liste öğelerini hazırla
                            val rotaInfoList = rotaList.mapIndexed { index, rota ->
                                val distance = calculateDistance(myKonum, LatLng(rota.enlemRota, rota.boylamRota))
                                "${index + 1}. ${rota.nameRota}"
                            }.toTypedArray() // Array for AlertDialog checkboxes

                            // AlertDialog oluştur
                            val builder = AlertDialog.Builder(this@MapsActivity)

                            builder.setTitle("Rotalar")
                                //.setMessage("Gezdiğin yerleri işaretle ve listeden çıkar. ")
                                .setMultiChoiceItems(rotaInfoList, null) { _, which, isChecked ->

                                }

                            builder.setPositiveButton("Tamam") { dialog, which ->
                                val checkedItems = (dialog as AlertDialog).listView.checkedItemPositions
                                val selectedRoutes = rotaList.filterIndexed { index, _ ->
                                    checkedItems[index]
                                }

                                // Remove selected routes from the map and database
                                selectedRoutes.forEach { route ->

                                    database
                                        .collection("usersRota")
                                        .document(auth.currentUser?.email!!)
                                        .collection("selectedPlaces")
                                        .document(route.documentId)
                                        .delete()
                                        .addOnSuccessListener {
                                            // Rota silindikten sonra haritayı güncelle

                                            updateMapAfterRemovingRoutes(selectedLocations, myKonum)
                                        }

                                    val tarih = Timestamp.now()
                                    val hashMap = hashMapOf(
                                        "name" to route.nameRota,
                                        "url" to route.urlGorselRota,
                                        "tarih" to tarih
                                    )

                                    updateMapAfterRemovingRoutes(selectedLocations, myKonum)

                                    database
                                        .collection("GezilenYerler")
                                        .document(auth.currentUser?.email!!)
                                        .collection("Seyahatlerim")
                                        .document(route.documentId)
                                        .set(hashMap)

                                    // Update selectedLocations list after removal
                                    selectedLocations.clear() // Clear existing locations
                                    database.collection("usersRota")
                                        .document(auth.currentUser?.email!!)
                                        .collection("selectedPlaces")
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            for (document in documents) {
                                                val enlem = document.getString("enlem")?.toDouble()
                                                val boylam = document.getString("boylam")?.toDouble()
                                                if (enlem != null && boylam != null) {
                                                    selectedLocations.add(LatLng(enlem, boylam))
                                                }
                                            }

                                            // Redraw map with updated locations
                                            mMap.clear() // Clear map markers and polylines
                                            val sortedLocations = listOf(myKonum) + selectedLocations.sortedBy {
                                                calculateDistance(myKonum, it)
                                            }

                                            // Reset totalDistance and totalDuration
                                            totalDistance = 0f
                                            totalDuration = 0L

                                            for (i in 0 until sortedLocations.size - 1) {
                                                drawRoute(sortedLocations[i], sortedLocations[i + 1])
                                            }
                                            addMarkers(selectedLocations + listOf(myKonum))
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e(TAG, "Error getting documents: ", exception)
                                        }
                                }
                            }

                            builder.setNegativeButton("İptal") { dialog, which ->
                                dialog.dismiss()
                            }

                            builder.setCancelable(false)
                            builder.show()
                        }
                    }

                    if (location.latitude == 0.0 || location.longitude == 0.0) {
                        Toast.makeText(this@MapsActivity, "Konum bulunmadı", Toast.LENGTH_LONG).show()
                        binding.progressBar7.visibility = android.view.View.GONE
                        binding.listeGoster.isEnabled = false
                    } else {
                        mMap.clear() // mevcut işaretçileri temizle
                        binding.progressBar7.visibility = android.view.View.GONE
                        binding.listeGoster.isEnabled = true
                        mMap.addMarker(MarkerOptions().position(myKonum).title("Mevcut Konumum"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myKonum, 16f))

                        // Initialize selectedLocations list
                        selectedLocations.clear() // Clear existing locations
                        database.collection("usersRota")
                            .document(auth.currentUser?.email!!)
                            .collection("selectedPlaces")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (document in documents) {
                                    val enlem = document.getString("enlem")?.toDouble()
                                    val boylam = document.getString("boylam")?.toDouble()
                                    if (enlem != null && boylam != null) {
                                        selectedLocations.add(LatLng(enlem, boylam))
                                    }
                                }

                                try {
                                    if (!selectedLocations.isNullOrEmpty()) {
                                        val sortedLocations = listOf(myKonum) + selectedLocations.sortedBy {
                                            calculateDistance(myKonum, it)
                                        }

                                        // Burada totalDistance ve totalDuration değerlerini sıfırlıyoruz
                                        totalDistance = 0f
                                        totalDuration = 0L

                                        for (i in 0 until sortedLocations.size - 1) {
                                            drawRoute(sortedLocations[i], sortedLocations[i + 1])
                                        }
                                        addMarkers(selectedLocations + listOf(myKonum))
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Error getting documents: ", exception)
                            }
                    }
                }
            }
        }

    }

    private fun updateMapAfterRemovingRoutes(selectedLocations: List<LatLng>, myLocation: LatLng) {
        mMap.clear() // Harita üzerindeki işaretçileri ve yolları temizle

        // Tüm konumlar için işaretçiler ekleyin (mevcut konum dahil)
        addMarkers(selectedLocations + listOf(myLocation))

        // Konumları sırala ve yolları yeniden çiz
        val sortedLocations = listOf(myLocation) + selectedLocations.sortedBy {
            calculateDistance(myLocation, it)
        }

        mMap.addMarker(MarkerOptions().position(myLocation).title("Mevcut Konumum"))

        // Toplam mesafeyi ve süreyi sıfırla
        totalDistance = 0f
        totalDuration = 0L

        for (i in 0 until sortedLocations.size - 1) {
            drawRoute(sortedLocations[i], sortedLocations[i + 1])
        }
    }
/*
    // Rotaları kaldırdıktan sonra haritayı güncelleyen metod
    private fun updateMapAfterRemovingRoutes(rotaList: List<RotaListModel>, konum: LatLng) {
        val myKonum = konum // Get current location

        // Clear previous markers and polylines
        mMap.clear()

        // Re-add current location marker
        mMap.addMarker(MarkerOptions().position(myKonum).title("Mevcut Konumum"))

        // Sort locations and draw routes
        val sortedLocations = rotaList.map { LatLng(it.enlemRota, it.boylamRota) }
        sortedLocations.forEachIndexed { index, location ->
            if (index == 0) {
                // Add marker for the first location in the sorted list
                mMap.addMarker(MarkerOptions().position(location).title("Mevcut Konumum"))
            } else {
                // Draw route between consecutive locations
                drawRoute(sortedLocations[index - 1], location)
            }
        }

        // Add markers for all locations (including current location)
        addMarkers(sortedLocations + listOf(myKonum))
    }

 */




    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            startLocationUpdates()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gpsEnabled || networkEnabled
    }

    private fun calculateDistance(location1: LatLng, location2: LatLng): Float {
        val result = floatArrayOf(0f)
        Location.distanceBetween(location1.latitude, location1.longitude, location2.latitude, location2.longitude, result)
        return result[0] / 1000 // Metreyi kilometreye çevir
    }

    private fun drawRoute(origin: LatLng, destination: LatLng) {
        val apiKey = "AIzaSyD6KRKlUdjH7TcM3B2S13iqQtqkYqi10DU"

        println("Origin : $origin Destination : $destination")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val directionsResult = DirectionsApi.newRequest(getGeoApiContext(apiKey))
                    .mode(TravelMode.DRIVING)
                    .origin("${origin.latitude},${origin.longitude}")
                    .destination("${destination.latitude},${destination.longitude}")
                    .await()

                val polylineOptions = PolylineOptions()
                    .color(0xFF6A8385.toInt()) // Çizgi rengi
                    .width(11f) // Çizgi kalınlığı

                for (step in directionsResult.routes[0].legs[0].steps) {
                    val points = step.polyline.decodePath()
                    for (point in points) {
                        polylineOptions.add(LatLng(point.lat, point.lng))
                    }
                }

                //val durationInMinutes = directionsResult.routes[0].legs[0].duration.inSeconds / 60

                totalDuration += directionsResult.routes[0].legs[0].duration.inSeconds / 60
                val formattedDuration = formatDurationToTurkish(totalDuration)

                totalDistance += directionsResult.routes[0].legs[0].distance.inMeters / 1000f

                val stringMesafe = String.format("%.2f", totalDistance)

                launch(Dispatchers.Main) {
                    mMap.addPolyline(polylineOptions)
                    binding.textView9.text = "Toplam Süre : \n  ${formattedDuration}"
                    binding.textView8.text = "Toplam Mesafe : \n ${stringMesafe} km"
                    val bounds = LatLngBounds.builder().include(origin).include(destination).build()
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                }
            } catch (e: ApiException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun formatDurationToTurkish(durationInMinutes: Long): String {
        val hours = durationInMinutes / 60
        val minutes = durationInMinutes % 60

        return if (hours > 0) {
            "${hours} saat ${minutes} dakika"
        } else {
            "${minutes} dakika"
        }
    }

    private fun getGeoApiContext(apiKey: String): GeoApiContext {
        return GeoApiContext.Builder()
            .apiKey(apiKey)
            .build()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 60000 // 1 dakika
            //Konum güncellemelerinin alınma süresi. Burada, 60.000 milisaniye (1 dakika) olarak ayarlanmıştır.
            fastestInterval = 5000 // 5 saniye
            //Konum güncellemelerinin alınma süresi için minimum süre. Bu değer, 5.000 milisaniye (5 saniye) olarak belirlenmiştir.
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun addMarkers(locations: List<LatLng>) {
        for (location in locations) {
            db.getRota {
                listKonum = it

                for (i in 0 until listKonum.size) {
                    if (listKonum[i].enlemRota == location.latitude && listKonum[i].boylamRota == location.longitude) {
                        val baslik = listKonum[i].nameRota
                        mMap.addMarker(MarkerOptions().position(location).title(baslik))

                        println(baslik)

                        mMap.setOnMarkerClickListener {
                            it.showInfoWindow()
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, 14f))
                            true
                        }
                    }
                }
            }
        }
    }
}
