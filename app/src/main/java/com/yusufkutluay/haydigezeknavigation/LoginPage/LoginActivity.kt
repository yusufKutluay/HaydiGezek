package com.yusufkutluay.haydigezeknavigation.LoginPage

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.yusufkutluay.haydigezeknavigation.AlertDiaolog.Uyari
import com.yusufkutluay.haydigezeknavigation.Firebase.FirestoreDatabase
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.databinding.ActivityLoginBinding
import com.yusufkutluay.haydigezeknavigation.main.FragmentFirstActivity

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private lateinit var auth : FirebaseAuth
    val database = FirestoreDatabase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uyari = Uyari()
        uyari.kontrolEtEthernet(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null){
            val intent = Intent(applicationContext,FragmentFirstActivity::class.java)
            startActivity(intent)
            finish()
        }


        var isImageChanged = false

        binding.sifreGoster.setOnClickListener {


            if (isImageChanged) {
                binding.sifreText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.sifreGoster.setImageResource(R.drawable.visibiliity_of)
            } else {
                binding.sifreText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.sifreGoster.setImageResource(R.drawable.visibility)
                binding.sifreText.typeface = binding.epostaKullaniciText.typeface

            }
            binding.sifreText.setSelection(binding.sifreText.text.length) // İmleci sonuna taşı
            isImageChanged = !isImageChanged


        }

    }

    fun girisBas(view: View){

        val email = binding.epostaKullaniciText.text.toString()
        val password = binding.sifreText.text.toString()

        //null gitmemesi için
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(applicationContext,"Lütfen tüm alanları doldurun.",Toast.LENGTH_LONG).show()
            return
        }

        //burda kullanıcı varmı diye kontrol etmek
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {

                if (it.isSuccessful){
                    val intent = Intent(applicationContext,FragmentFirstActivity::class.java)
                    Toast.makeText(this,"Hoşgeldiniz ${auth.currentUser!!.email}",Toast.LENGTH_LONG).show()
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Hatalı giriş yaptınız !!",Toast.LENGTH_LONG).show()
            }

    }

    fun uyeOlYazisi(view: View){

        val intent = Intent(this,SignupActivity::class.java)
        startActivity(intent)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Do nothing to prevent going back to the previous activity
        finishAffinity()// Uygulamayı direk kapatır böylece başka sayfalara gitmesi engellenir
    }

}