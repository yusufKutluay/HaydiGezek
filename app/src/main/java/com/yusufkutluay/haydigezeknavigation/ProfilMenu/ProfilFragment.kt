package com.yusufkutluay.haydigezeknavigation.ProfilMenu

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yusufkutluay.haydigezeknavigation.Adapter.FeedRecyclerAdapter
import com.yusufkutluay.haydigezeknavigation.Firebase.FirestoreDatabase
import com.yusufkutluay.haydigezeknavigation.LoginPage.LoginActivity
import com.yusufkutluay.haydigezeknavigation.Model.Post
import com.yusufkutluay.haydigezeknavigation.Model.UsersModel
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.databinding.FragmentProfilBinding
import java.util.Date


class ProfilFragment : Fragment() {

    private lateinit var binding: FragmentProfilBinding
    private lateinit var auth : FirebaseAuth
    val db = FirestoreDatabase()
    private var list = ArrayList<UsersModel>()
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
        binding = FragmentProfilBinding.inflate(inflater,container,false)
        return binding.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db.getUsers {

            list = it

            for (i in 0 until list.size){

                if (auth.currentUser?.email == list[i].email){
                    binding.userNameProfil.text = list[i].name
                }

            }

        }

        binding.cikisYap.setOnClickListener {
            cikisYap(it)
        }

        binding.gonderiBas.setOnClickListener {
            navController.navigate(R.id.action_profilFragment_to_gonderiFragment)
        }

        binding.seyahat.setOnClickListener {
            navController.navigate(R.id.action_profilFragment_to_seyahatlerimFragment)
        }



    }

    fun cikisYap(view: View){
        //çıkış yapınca authenticationdan da çıkış yapıldı

        val build = AlertDialog.Builder(requireContext())
        build.setTitle("Çıkış")
        build.setMessage("Çıkmak istediğine emin misin?")
        build.setPositiveButton("Evet", DialogInterface.OnClickListener { dialog, which ->

            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)

        })
        build.setNegativeButton("Hayır", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        build.setCancelable(false)
        build.create().show()
    }




}