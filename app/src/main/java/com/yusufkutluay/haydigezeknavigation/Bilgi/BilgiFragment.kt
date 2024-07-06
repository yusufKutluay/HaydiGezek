package com.yusufkutluay.haydigezeknavigation.Bilgi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.yusufkutluay.haydigezeknavigation.Firebase.FirestoreDatabase
import com.yusufkutluay.haydigezeknavigation.Model.BilgiModel
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.databinding.FragmentBilgiBinding
import java.net.URLEncoder

class BilgiFragment : Fragment() {

    private lateinit var binding: FragmentBilgiBinding
    private var isim: String? = null
    private val db = FirestoreDatabase()
    private var bilgiList = ArrayList<BilgiModel>()
    private lateinit var database: FirebaseFirestore
    private var name: String? = null
    private var fragmentName : String? = null
    private lateinit var navController: NavController
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseFirestore.getInstance()
        arguments?.let {
            isim = it.getString("rotaName")
            fragmentName = it.getString("fragment")
            println(isim)
        }

        auth = FirebaseAuth.getInstance()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBilgiBinding.inflate(inflater, container, false)
        binding.progressBar4.visibility = View.VISIBLE
        binding.imageView13.visibility = View.VISIBLE
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        navController = findNavController()

        // Geri tuşuna basıldığında önceki fragment'a dönüş işlemini yönet
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (fragmentName == "HomeFragment") {
                val action = BilgiFragmentDirections.actionBilgiFragmentToHomeFragment()
                navController.navigate(action)
            } else if (fragmentName == "SehirFragment") {
                val action = BilgiFragmentDirections.actionBilgiFragmentToSehirFragment(name!!)
                navController.navigate(action)
            }
        }

        isim?.let { placeName ->
            findCityForPlace(placeName) { cityName ->
                if (cityName != null) {

                    name = cityName
                    db.getBilgi(cityName, placeName) { bilgiList ->
                        this.bilgiList = bilgiList
                        if (bilgiList.isNotEmpty()) {
                            val aciklama = bilgiList[0].aciklama
                            binding.textBilgi.setText(aciklama)
                            println("---------------------------------------")
                            println(aciklama)
                            println(bilgiList[0].url)

                            Picasso.get().load(bilgiList[0].url).into(binding.imageBilgi)
                            binding.progressBar4.visibility = View.GONE
                            binding.imageView13.visibility = View.GONE


                        } else {
                            println("Bilgi bulunamadı.")
                        }
                    }
                } else {
                    println("City not found for Place: $placeName")
                }
            }

            val user = auth.currentUser?.email
            var isRotaEkleClicked = false

            // Kullanıcının seçtiği yerler koleksiyonunu al
            val selectedPlacesRef = database.collection("usersRota").document(user!!)
                .collection("selectedPlaces")

            // Veritabanında bu yeri daha önce ekleyip eklenmediğini kontrol et
            selectedPlacesRef.whereEqualTo("name", placeName).get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Zaten bu yeri eklemişse işlem yapma
                    binding.rotaEkle.text = "Rotaya eklendi "
                    binding.rotaEkle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_24px, 0)
                    return@addOnSuccessListener
                }else{
                    binding.rotaEkle.text = "Rotaya ekle "
                    binding.rotaEkle.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        R.drawable.paylas, 0)


                }

            binding.rotaEkle.setOnClickListener {
                binding.rotaEkle.text = "Rotaya eklendi"
                binding.rotaEkle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_24px, 0)

                if (!isRotaEkleClicked) {
                    isRotaEkleClicked = true

                    // Eğer yoksa, yeri rotaya ekle
                    database.collection("Sehirler").get().addOnSuccessListener { cityDocuments ->
                        for (document in cityDocuments) {
                            document.reference.collection("GezilecekYerler").whereEqualTo("name", placeName).get()
                                .addOnSuccessListener { placeDocuments ->
                                    for (i in placeDocuments) {
                                        val hashMap = hashMapOf(
                                            "name" to i.getString("name"),
                                            "enlem" to i.getString("enlem"),
                                            "boylam" to i.getString("boylam"),
                                            "url" to i.getString("url")
                                        )

                                        val encodedPlac = URLEncoder.encode(i.getString("name"), "UTF-8")
                                        val encodedPlaceName = encodedPlac.lowercase()

                                        selectedPlacesRef.document(encodedPlaceName)
                                            .set(hashMap)
                                            .addOnSuccessListener {
                                                Toast.makeText(requireContext(), "${i.getString("name")} rotaya eklendi.", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                println("Error adding place to user's route: $e")
                                            }
                                    }
                                }
                        }
                    }


                    }
                }
            }

            binding.nameBilgi.text = placeName
         } ?: run {
            println("Place name is null.")
        }

        binding.geriDon.setOnClickListener {
            callback.remove()
            if (fragmentName == "HomeFragment"){
                val action = BilgiFragmentDirections.actionBilgiFragmentToHomeFragment()
                navController.navigate(action)
            }else if (fragmentName == "SehirFragment"){
                val action = BilgiFragmentDirections.actionBilgiFragmentToSehirFragment(name!!)
                navController.navigate(action)
            }

        }
    }


    private fun findCityForPlace(placeName: String, callback: (String?) -> Unit) {
        database.collection("Sehirler").get().addOnSuccessListener { cityDocuments ->
            for (cityDocument in cityDocuments) {
                val cityName = cityDocument.id
                cityDocument.reference.collection("GezilecekYerler").whereEqualTo("name", placeName).get()
                    .addOnSuccessListener { placeDocuments ->
                        if (!placeDocuments.isEmpty) {
                            callback(cityName)
                            return@addOnSuccessListener
                        }
                    }
                    .addOnFailureListener { exception ->
                        println("Error fetching places: $exception")
                    }
            }
            callback(null) // Şehir bulunamazsa null döner
        }.addOnFailureListener { exception ->
            println("Error fetching cities: $exception")
            callback(null)
        }
    }
}
