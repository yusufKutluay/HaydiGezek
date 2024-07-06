package com.yusufkutluay.haydigezeknavigation.ProfilMenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yusufkutluay.haydigezeknavigation.Adapter.FeedRecyclerAdapter
import com.yusufkutluay.haydigezeknavigation.Adapter.RecyclerSeyahat
import com.yusufkutluay.haydigezeknavigation.Model.SeyahatModel
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.databinding.FragmentSeyahatlerimBinding
import java.util.Date


class SeyahatlerimFragment : Fragment() {

    private lateinit var binding : FragmentSeyahatlerimBinding
    private lateinit var db  : FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private val seyahatList = ArrayList<SeyahatModel>()
    private lateinit var recylerViewAdapter : RecyclerSeyahat
    private val navController by lazy { findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSeyahatlerimBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        verileriAl()

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        recylerViewAdapter = RecyclerSeyahat(seyahatList)
        binding.recyclerView.adapter = recylerViewAdapter

        binding.geriDon.setOnClickListener {
            navController.navigate(R.id.action_seyahatlerimFragment_to_profilFragment)

        }


    }

    fun verileriAl(){

        db.collection("GezilenYerler")
            .document(auth.currentUser?.email!!)
            .collection("Seyahatlerim")
            .get()
            .addOnSuccessListener {
                for (i in it){

                    val name = i.getString("name")
                    val url = i.getString("url")
                    val tarih = i.getTimestamp("tarih") ?: Timestamp(Date())

                    if (name != null && url != null && tarih != null){
                        val seyahat = SeyahatModel(name,url,tarih)
                        seyahatList.add(seyahat)
                    }



                }
                recylerViewAdapter.notifyDataSetChanged()
            }

    }
}