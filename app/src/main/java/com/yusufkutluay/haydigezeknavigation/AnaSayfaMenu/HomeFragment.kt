package com.yusufkutluay.haydigezeknavigation.AnaSayfaMenu

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.yusufkutluay.haydigezeknavigation.Adapter.RecyclerRotaAdapter
import com.yusufkutluay.haydigezeknavigation.Adapter.RecyclerSehirler
import com.yusufkutluay.haydigezeknavigation.Firebase.FirestoreDatabase
import com.yusufkutluay.haydigezeknavigation.Model.RotaModel
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.databinding.FragmentHomeBinding
import java.util.Locale


class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentHomeBinding
    private var rotaList = ArrayList<RotaModel>()
    private var db = FirestoreDatabase()
    private lateinit var recylerViewAdapter: RecyclerRotaAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerSehirAdapter : RecyclerSehirler
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var navController: NavController

    private val imageList = listOf(
        R.drawable.istanbul,
        R.drawable.ankara,
        R.drawable.sivasmeydan,
        R.drawable.diyarbakir
    )

    private var currentImageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        navController = findNavController()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater,container,false)
        progressBar = binding.progressBar3
        progressBar.visibility = View.VISIBLE // İlerleme çubuğunu başlangıçta görünür hale getirin
        binding.yukleme.visibility = View.VISIBLE
        binding.root.setBackgroundColor(resources.getColor(R.color.white)) // Arka plan rengini beyaza ayarlayın
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db.addFirestore()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        /// Konum izni kontrolü ve konum bilgisini alma
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        getCityName(latitude, longitude) { cityName ->
                            // cityName null olabilir, bu yüzden ?.let ile güvenli bir şekilde işleyin
                            cityName?.let { city ->
                                fetchRotaData(city)
                            } ?: run {
                                // Konum bulunamazsa varsayılan verileri yükle
                                loadDefaulData()
                                konumUyari()
                            }
                        }
                    }else{
                        loadDefaulData()
                        konumUyari()
                    }
                }
        } else {
            // İzin yoksa izin iste
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        recylerSehirVerileri()


        binding.profilGit.setOnClickListener {

            navController.navigate(R.id.action_homeFragment_to_profilFragment3)

        }

        binding.resimDegis.setOnClickListener {

            changeImage()

        }

    }

    private fun changeImage(){

        currentImageIndex = (currentImageIndex + 1) % imageList.size
        binding.imageView9.setImageResource(imageList[currentImageIndex])

    }

    private fun recylerSehirVerileri(){
        db.sehir {

            val layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
            binding.recyclerSehirler.layoutManager = layoutManager
            recyclerSehirAdapter = RecyclerSehirler(it)
            binding.recyclerSehirler.adapter = recyclerSehirAdapter
            binding.yukleme.visibility = View.GONE

        }
    }

    private fun recyclerViewDocument(list : ArrayList<RotaModel>){

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerRotalar.layoutManager = layoutManager
        recylerViewAdapter = RecyclerRotaAdapter(list)
        binding.recyclerRotalar.adapter = recylerViewAdapter

        progressBar.visibility = View.GONE

    }

    private fun konumUyari(){

        Toast.makeText(requireContext(),"Lütfen konumunuzu açınız !",Toast.LENGTH_LONG).show()

    }

    private fun fetchRotaData(cityName: String) {
        db.getVeri(cityName.toLowerCase()) { dataList ->
            if (dataList.isEmpty()){
                loadDefaulData()
            }else{
                rotaList.clear()
                for (data in dataList) {
                    rotaList.add(RotaModel(data.name,data.urlGorsel))
                }

                recyclerViewDocument(rotaList)
            }
            }

    }

    private fun loadDefaulData(){
        db.getVeri("istanbul") { dataList ->
            rotaList.clear()
            for (data in dataList) {
                rotaList.add(RotaModel(name = data.name, url = data.urlGorsel))
            }

            recyclerViewDocument(rotaList)


        }
    }

    private fun getCityName(latitude: Double, longitude: Double, callback: (String?) -> Unit) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses!!.isNotEmpty()) {
            val cityName = addresses[0].adminArea // adminArea veya locality kullanılabilir
            binding.textView2.text = cityName // UI güncellemesi yapabilirsiniz
            callback(cityName)
        } else {
            callback(null)
        }
    }


}