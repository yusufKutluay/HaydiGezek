package com.yusufkutluay.haydigezeknavigation.Sehirler

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yusufkutluay.haydigezeknavigation.Adapter.RecyclerAdapter
import com.yusufkutluay.haydigezeknavigation.AlertDiaolog.Uyari
import com.yusufkutluay.haydigezeknavigation.Firebase.FirestoreDatabase
import com.yusufkutluay.haydigezeknavigation.Model.Place
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.databinding.FragmentSehirBinding


class SehirFragment : Fragment() {

    private lateinit var binding: FragmentSehirBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var recylerViewAdapter: RecyclerAdapter
    private val data = FirestoreDatabase()
    var sehirName : String? = null
    private val navController by lazy { findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSehirBinding.inflate(inflater,container,false)
        binding.progressBar2.visibility = View.VISIBLE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var kelime = " "

        kelime.replaceFirstChar { it.lowercase() }

        arguments?.let {
            sehirName = it.getString("name")?.let { name ->
                name.replaceFirstChar { it.lowercase() }
                    .replace('İ', 'i')
                    .replace('ı', 'i')
                    .replace('Ğ', 'g')
                    .replace('ğ', 'g')
                    .replace('Ü', 'u')
                    .replace('ü', 'u')
                    .replace('Ş', 's')
                    .replace('ş', 's')
                    .replace('Ö', 'o')
                    .replace('ö', 'o')
                    .replace('Ç', 'c')
                    .replace('ç', 'c')
            }
        }
        if (sehirName == "i̇stanbul"){
            sehirName = "istanbul"
        }

        var textSehir = sehirName!!
        textSehir = textSehir.replaceFirstChar { it.uppercase() }

        binding.sehirName.text = textSehir
        println(sehirName)


        data.getVeri(sehirName!!) {
            // Fetch data from Firestore

            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.recyclerView.layoutManager = layoutManager
            recylerViewAdapter = RecyclerAdapter(it)
            binding.recyclerView.adapter = recylerViewAdapter

            binding.progressBar2.visibility = View.GONE

        }

        val uyari = Uyari()
        uyari.kontrolEtEthernet(requireContext())


        binding.rotaEkle.setOnClickListener { rotaEkle(it) }

        binding.backHome.setOnClickListener {
            navController.navigate(R.id.action_sehirFragment_to_homeFragment)
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.navigate(R.id.action_sehirFragment_to_homeFragment)
            }
        })

    }

    private fun rotaEkle(view: View) {

        val build = android.app.AlertDialog.Builder(requireContext())
        build.setTitle("Rota")
        build.setMessage("Seçtikleriniz rotaya eklensin mi?")
        build.setPositiveButton("Evet", DialogInterface.OnClickListener{ dialog, which ->
            navController.navigate(R.id.action_sehirFragment_to_homeFragment)
            Toast.makeText(requireContext(),"Rotaya eklendi!", Toast.LENGTH_LONG).show()
        })
        build.setNegativeButton("Hayır", DialogInterface. OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        build.setCancelable(false)
        build.show()

    }

}