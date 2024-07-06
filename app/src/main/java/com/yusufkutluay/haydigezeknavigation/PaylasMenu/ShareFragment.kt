package com.yusufkutluay.haydigezeknavigation.PaylasMenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yusufkutluay.haydigezeknavigation.Adapter.FeedRecyclerAdapter
import com.yusufkutluay.haydigezeknavigation.Model.Post
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.databinding.FragmentShareBinding
import java.util.Date


class ShareFragment : Fragment() {

    private lateinit var binding: FragmentShareBinding
    private val navController by lazy { findNavController() }
    private lateinit var recylerViewAdapter : FeedRecyclerAdapter
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    var postListesi = ArrayList<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentShareBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.floatingActionButton3.setOnClickListener {
            navController.navigate(R.id.action_shareFragment_to_yazPaylasFragment)
        }

        verileriAl()

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        recylerViewAdapter = FeedRecyclerAdapter(postListesi)
        binding.recyclerView.adapter = recylerViewAdapter


    }

    fun verileriAl(){

        db.collection("Post")
            .orderBy("tarih", Query.Direction.DESCENDING)// tarihe göre sırala demek
            .addSnapshotListener { snapshot, exception ->

                if (exception != null){
                    Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_LONG).show()
                }else{
                    if (snapshot != null){
                        if (!snapshot.isEmpty){
                            val documents = snapshot.documents

                            postListesi.clear()

                            for (document in documents){
                                val kullaniciEmail = document.get("email") as String
                                val kullaniciYorum = document.get("yorum") as String
                                val gorselUrl = document.get("url") as String
                                val tarih = document.getTimestamp("tarih") ?: Timestamp(Date())
                                val indirilenPost = Post(kullaniciEmail,kullaniciYorum,gorselUrl,tarih)
                                postListesi.add(indirilenPost)
                            }
                            recylerViewAdapter.notifyDataSetChanged()  //yeni veri geldi kendini yenile demek
                        }
                    }
                }

            }

    }


}