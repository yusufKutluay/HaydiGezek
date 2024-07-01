package com.yusufkutluay.haydigezeknavigation.Adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.yusufkutluay.haydigezeknavigation.Model.MapModel
import com.yusufkutluay.haydigezeknavigation.Model.Place
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.Sehirler.SehirFragmentDirections
import com.yusufkutluay.haydigezeknavigation.databinding.RecyclerRowPlaceBinding
import java.net.URLEncoder




class RecyclerAdapter(val postList: ArrayList<Place>) : RecyclerView.Adapter<RecyclerAdapter.PlaceHolder>() {

    var listMaps = ArrayList<MapModel>()
    private val db = Firebase.firestore
    val selectedPlaces = mutableSetOf<String>() // Seçilen yer isimlerini saklamak için set
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser!!.email
    val userSelectedPlacesRef = db.collection("usersRota").document(user!!).collection("selectedPlaces")


    class PlaceHolder(val binding : RecyclerRowPlaceBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {

        val binding = RecyclerRowPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {

        val place = postList[position]

        //gezilecek yer ismi eklendi
        holder.binding.recyclerText.text = place.name
        // Boolean bir değişken oluşturun
        var isImageChanged = false

        val listeEkle = MapModel(place.enlem,place.boylam,place.name)

        // Place adını kodlayın
        // Place adını kodlayın
        val encodedPlac = URLEncoder.encode(place.name, "UTF-8")
        val encodedPlaceName = encodedPlac.lowercase()

        val hashMap = hashMapOf(
            "name" to place.name,
            "enlem" to place.enlem.toString(),
            "boylam" to place.boylam.toString(),
            "url" to place.urlGorsel
        )




        // Adaptör ilk kurulduğunda her yer için seçili durumu kontrol edin
        checkSelectedState(encodedPlaceName) { isSelected ->
            if (isSelected) {
                selectedPlaces.add(encodedPlaceName)
                holder.binding.checkbox.setImageResource(R.drawable.check_close)  // Seçili olarak işaretle
            } else {
                holder.binding.checkbox.setImageResource(R.drawable.check_open)  // Seçili değil olarak işaretle
            }
        }



        holder.binding.checkbox.setOnClickListener {
            val isSelected = selectedPlaces.contains(encodedPlaceName)
            if (isSelected) {
                holder.binding.checkbox.setImageResource(R.drawable.check_open)
                selectedPlaces.remove(encodedPlaceName)
                listMaps.remove(listeEkle)
                db.collection("usersRota").document(user!!).collection("selectedPlaces").document(encodedPlaceName).delete()
            } else {
                holder.binding.checkbox.setImageResource(R.drawable.check_close)
                selectedPlaces.add(encodedPlaceName)
                listMaps.add(listeEkle)
                db.collection("usersRota").document(user!!).collection("selectedPlaces").document(encodedPlaceName).set(hashMap)
            }
        }


        holder.binding.recyclerImage.setOnClickListener {
            val navController = holder.itemView.findNavController()
            val action = SehirFragmentDirections.actionSehirFragmentToBilgiFragment(place.name,"SehirFragment")
            navController.navigate(action)
        }



        // picasso yöntemiyle url yi ImageView a aktarma
        Picasso.get().load(postList[position].urlGorsel).into(holder.binding.recyclerImage)

    }

    private fun checkSelectedState(placeName: String, callback: (Boolean) -> Unit) {
        userSelectedPlacesRef.document(placeName)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                callback(documentSnapshot.exists())
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }
    }

}