package com.yusufkutluay.haydigezeknavigation.Adapter

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import com.squareup.picasso.Picasso
import com.yusufkutluay.haydigezeknavigation.Model.RotaListModel
import com.yusufkutluay.haydigezeknavigation.databinding.RecyclerListeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
class RecyclerListeAdapter(val postList: ArrayList<RotaListModel>) : RecyclerView.Adapter<RecyclerListeAdapter.ListHolder>() {

    val db = Firebase.firestore
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    class ListHolder(val binding: RecyclerListeBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var roundedDistance : String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListHolder {
        val binding = RecyclerListeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onBindViewHolder(holder: ListHolder, position: Int) {
        val selectedLocation = LatLng(postList[position].enlemRota, postList[position].boylamRota)
        val locationManager = holder.itemView.context.getSystemService(LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(holder.itemView.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val currentLocation = locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
            currentLocation?.let {
                val myLocation = LatLng(it.latitude, it.longitude)
                drawRoute(myLocation, selectedLocation, holder)
            }
        } else {
            ActivityCompat.requestPermissions(holder.itemView.context as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        holder.binding.textName.text = postList[position].nameRota
        Picasso.get().load(postList[position].urlGorselRota).into(holder.binding.imageRota)

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.email

        holder.binding.listeCikar.setOnClickListener {
            coroutineScope.launch {
                val itemId = postList[position].documentId
                try {
                    db.collection("usersRota").document(userId!!).collection("selectedPlaces").document(itemId).delete().await()
                    postList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, postList.size)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }



    private fun drawRoute(origin: LatLng, destination: LatLng, holder: ListHolder) {
        val apiKey = "AIzaSyD6KRKlUdjH7TcM3B2S13iqQtqkYqi10DU"

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val directionsResult = DirectionsApi.newRequest(getGeoApiContext(apiKey))
                    .mode(TravelMode.DRIVING)
                    .origin("${origin.latitude},${origin.longitude}")
                    .destination("${destination.latitude},${destination.longitude}")
                    .await()

                if (directionsResult.routes.isNotEmpty() && directionsResult.routes[0].legs.isNotEmpty()) {
                    val distanceInMeters = directionsResult.routes[0].legs[0].distance.inMeters
                    val distanceInKm = distanceInMeters / 1000.0
                    roundedDistance = distanceInKm.toInt().toString()
                    withContext(Dispatchers.Main) {
                        holder.binding.textView12.text = "Mesafe: $roundedDistance km"
                    }
                }

                val durationInMinutes = directionsResult.routes[0].legs[0].duration.inSeconds / 60
                val formattedDuration = formatDurationToTurkish(durationInMinutes)
                withContext(Dispatchers.Main) {
                    holder.binding.textView21.text = "Süre : $formattedDuration"
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

    private fun getGeoApiContext(apiKey: String): GeoApiContext {
        return GeoApiContext.Builder()
            .apiKey(apiKey)
            .build()
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
}
