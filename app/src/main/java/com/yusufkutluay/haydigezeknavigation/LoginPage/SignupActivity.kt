package com.yusufkutluay.haydigezeknavigation.LoginPage


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.yusufkutluay.haydigezeknavigation.AlertDiaolog.Uyari
import com.yusufkutluay.haydigezeknavigation.Firebase.FirestoreDatabase
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.databinding.ActivitySignupBinding
import com.yusufkutluay.haydigezeknavigation.main.FragmentFirstActivity


class SignupActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignupBinding
    val db = FirestoreDatabase()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun uyeOlBas(view: View){

        val uyeAd = binding.uyeAd.text.toString()
        val uyeUserName = binding.uyeKullaniciAd.text.toString()
        val uyeEmail = binding.uyeEposta.text.toString()
        val uyeSifre = binding.uyeSifre.text.toString()


        if (uyeAd.isEmpty() || uyeEmail.isEmpty() || uyeUserName.isEmpty() || uyeSifre.isEmpty()){
            Toast.makeText(applicationContext,"Lütfen tüm alanları doldurun.",Toast.LENGTH_LONG).show()
            return
        }

        val uyari = Uyari()
        uyari.kontrolEtEthernet(this)

        auth.createUserWithEmailAndPassword(uyeEmail,uyeSifre)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    db.signUp(uyeAd,uyeUserName,uyeEmail,uyeSifre)
                    Toast.makeText(applicationContext,"Kayıt başarılı",Toast.LENGTH_LONG).show()
                    val intent = Intent(this,FragmentFirstActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Lütfen eksiksiz doldurun",Toast.LENGTH_LONG).show()
            }


    }




}