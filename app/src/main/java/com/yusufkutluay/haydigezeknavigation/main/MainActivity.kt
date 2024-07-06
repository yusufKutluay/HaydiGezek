package com.yusufkutluay.haydigezeknavigation.main

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.yusufkutluay.haydigezeknavigation.LoginPage.LoginActivity
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        timeClock()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //altbar rengi değiştirme işlemi
        val window = window
        window.navigationBarColor = ContextCompat.getColor(this,R.color.colorApp)
    }

    private fun timeClock() {

        // sayaca göre sayfanın açılışı ne kadar olacağını belirliyoruz

        //millislnFuture : Kaç saniye olacağı 1000 = 1sn
        // countDownInterval ise kaçar kaçar geri saymak demek

        val intent = Intent(applicationContext, LoginActivity::class.java)

        val timer = object : CountDownTimer(500, 500) {
            override fun onTick(millisUntilFinished: Long) {
                //burda süreyi bir yere aktarmak için
            }

            override fun onFinish() {

                if (auth.currentUser == null) {
                    Toast.makeText(
                        this@MainActivity,
                        "Hoşgeldiniz. Lütfen giriş yapınız. :)",
                        Toast.LENGTH_LONG
                    ).show()
                }
                startActivity(intent)
                finish()
            }

        }.start()

    }

}