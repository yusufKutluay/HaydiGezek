package com.yusufkutluay.haydigezeknavigation.PaylasMenu

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.databinding.FragmentYazPaylasBinding
import java.util.UUID


class YazPaylasFragment : Fragment() {
    private lateinit var binding: FragmentYazPaylasBinding
    private val navController by lazy { findNavController() }
    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null

    private lateinit var storage : FirebaseStorage
    private lateinit var auth : FirebaseAuth
    val database = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentYazPaylasBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.paylas.setOnClickListener {
         paylas(it)
        }

        binding.fotografEkle.setOnClickListener {
            fotografEkle(it)
        }


        binding.back.setOnClickListener {
            navController.navigate(R.id.action_yazPaylasFragment_to_shareFragment)
        }

    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.visibility = View.VISIBLE
    }

    fun paylas(view: View){


        //depo işlemleri
        //UUID -> universal uniqe id

        val uuid = UUID.randomUUID() //random değer atar
        val gorselIsmi = "${uuid}.png"
        val reference = storage.reference
        val gorselReference = reference.child("images").child(gorselIsmi)

        if (secilenGorsel != null){
            gorselReference.putFile(secilenGorsel!!).addOnSuccessListener {

                //görselin url sini almak istiyoruz
                gorselReference.downloadUrl.addOnSuccessListener { uri ->
                    // println(uri.toString())
                    val downladUrl = uri.toString()
                    val guncelKullaniciEmail = auth.currentUser!!.email.toString()
                    val kullaniciYorum = binding.yorumYaz.text.toString()

                    if (kullaniciYorum.isEmpty()){
                        Toast.makeText(requireContext(),"Lütfen birşeyler yazın!",Toast.LENGTH_LONG).show()
                    }
                    val tarih = Timestamp.now()

                    //veritabanı işlemleri
                    //veritabanına veri kaydetmek



                    val postHashMap = hashMapOf(
                        "url" to downladUrl,
                        "email" to guncelKullaniciEmail,
                        "yorum" to kullaniciYorum,
                        "tarih" to tarih
                    )




                    database.collection("Post")
                        .add(postHashMap)
                        .addOnCompleteListener { DocumentReference ->
                            if (DocumentReference.isSuccessful){
                                Toast.makeText(requireContext(),"Gönderin yüklendi <3", Toast.LENGTH_LONG).show()
                                navController.navigate(R.id.action_yazPaylasFragment_to_shareFragment)
                            }

                        }.addOnFailureListener { exception ->
                            Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_LONG).show()
                        }

                }
                //Toast.makeText(this,"Gönderin yüklendi <3",Toast.LENGTH_LONG).show()
                //intent ile geri gitmeye gerek yok
                //çünkü zaten geride duruyor buton olduğu için
                //val intent = Intent(this,FeedActivity::class.java)
                //startActivity(intent)
                //finish()

            }.addOnFailureListener{ exception ->
                Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }else{
            println("null")
        }

        if (secilenGorsel != null){
            binding.yorumYaz.isEnabled = false
            binding.fotografEkle.isEnabled = false
            hideKeyboard(view)
            binding.paylas.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            Toast.makeText(requireContext(),"Gönderin yükleniyor", Toast.LENGTH_LONG).show()
        }




    }

    fun hideKeyboard(view: View) {
        val inputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun fotografEkle(view: View){
        if (Build.VERSION.SDK_INT >= 32){
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent,2)
        }else{
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş izin almalıyız
                ActivityCompat.requestPermissions(requireContext() as Activity,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE) ,1)
            }else{
                //galeriye gidecez izin verildiyse
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }



    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == 1){
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //galeriye gidecez izin verildiyse
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }else{
                Toast.makeText(requireContext(),"Galeriye erişim izni verilmedi!", Toast.LENGTH_LONG).show()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            secilenGorsel = data.data
            if (secilenGorsel != null){
                if (Build.VERSION.SDK_INT >= 32){
                    val source = ImageDecoder.createSource(requireContext().contentResolver,secilenGorsel!!)
                    secilenBitmap = ImageDecoder.decodeBitmap(source)
                    binding.gorselSec.setImageBitmap(secilenBitmap)
                }else{
                    secilenBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver,secilenGorsel)
                    binding.gorselSec.setImageBitmap(secilenBitmap)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


}