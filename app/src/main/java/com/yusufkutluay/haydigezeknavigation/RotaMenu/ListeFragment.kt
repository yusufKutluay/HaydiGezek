package com.yusufkutluay.haydigezeknavigation.RotaMenu

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.yusufkutluay.haydigezeknavigation.Adapter.RecyclerListeAdapter
import com.yusufkutluay.haydigezeknavigation.Firebase.FirestoreDatabase
import com.yusufkutluay.haydigezeknavigation.Maps.MapsActivity
import com.yusufkutluay.haydigezeknavigation.Model.RotaListModel
import com.yusufkutluay.haydigezeknavigation.databinding.FragmentListeBinding

class ListeFragment : Fragment() {

    private lateinit var binding: FragmentListeBinding
    private lateinit var recylerViewAdapter: RecyclerListeAdapter
    private val db = FirestoreDatabase()
    private var rotaList = ArrayList<RotaListModel>()
    private val selectedLocations = mutableListOf<LatLng>()
    private var rotaMapList = ArrayList<RotaListModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListeBinding.inflate(inflater, container, false)
        recylerViewAdapter = RecyclerListeAdapter(ArrayList())
        binding.progressBar6.visibility = View.VISIBLE
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadRotaList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.listeMap.setOnClickListener { listeMap(it) }

    }

    private fun loadRotaList() {
        db.getRota { rotaListe ->
            binding.progressBar6.visibility = View.GONE
            if (rotaListe.isNotEmpty()) {
                rotaList = rotaListe

                val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.recyclerListe.layoutManager = layoutManager
                recylerViewAdapter = RecyclerListeAdapter(rotaList)
                binding.recyclerListe.adapter = recylerViewAdapter

                println(rotaList[0].enlemRota)
            } else {
                println("Rota listesi boş!")
                binding.textView9.text = "Rota için gezmek istediğiniz bir yer ekleyin...."
                rotaList.clear()
                recylerViewAdapter.notifyDataSetChanged()
            }
        }
    }

    fun listeMap(view: View){

        selectedLocations.clear()

        val build = AlertDialog.Builder(requireContext())
        build.setTitle("Uyarı")
        build.setMessage("Rota oluşturulsun mu?")
        build.setPositiveButton("Evet",DialogInterface.OnClickListener { dialog, which ->


            db.getRota {

                rotaMapList = it

                if (rotaMapList.isNotEmpty()){

                    for (i in 0 until rotaMapList.size){
                        //println(rotaMapList[i].enlemRota)
                        //selectedLocations.add(LatLng(rotaMapList[i].enlemRota, rotaMapList[i].boylamRota))
                    }
                    val intent = Intent(context, MapsActivity::class.java)
                    //intent.putParcelableArrayListExtra("selectedLocations", ArrayList(selectedLocations))
                    //intent.putExtra("sehirName","sivas")
                    startActivity(intent)

                }

            }

        })

        build.setNegativeButton("Hayır",DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })

        build.setCancelable(false)
        build.show()


    }

}




