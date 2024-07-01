package com.yusufkutluay.haydigezeknavigation.ProfilMenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yusufkutluay.haydigezeknavigation.Adapter.FeedRecyclerAdapter
import com.yusufkutluay.haydigezeknavigation.Firebase.FirestoreDatabase
import com.yusufkutluay.haydigezeknavigation.Model.Post
import com.yusufkutluay.haydigezeknavigation.Model.UsersModel
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.databinding.FragmentGonderiBinding
import java.util.Date


class GonderiFragment : Fragment() {

    private lateinit var binding: FragmentGonderiBinding
    private lateinit var auth : FirebaseAuth
    val db = FirestoreDatabase()
    private val database = Firebase.firestore
    private lateinit var adapter: FeedRecyclerAdapter
    private var postList = ArrayList<Post>()
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
        binding = FragmentGonderiBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backHome.setOnClickListener {
            navController.navigate(R.id.action_gonderiFragment_to_profilFragment)
        }

        setupRecyclerView()
        fetchUserPosts()

    }

    private fun setupRecyclerView() {
        adapter = FeedRecyclerAdapter(postList)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun fetchUserPosts() {
        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            database.collection("Post")
                .whereEqualTo("email", currentUserEmail)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val email = document.get("email") as String
                        val yorum = document.get("yorum") as String
                        val url = document.get("url") as String
                        val tarih = document.getTimestamp("tarih") ?: Timestamp(Date())

                        val postListesi = Post(email,yorum,url,tarih)

                        postList.add(postListesi)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    // Hata durumunda yapılacak işlemler
                }
        }
    }
}