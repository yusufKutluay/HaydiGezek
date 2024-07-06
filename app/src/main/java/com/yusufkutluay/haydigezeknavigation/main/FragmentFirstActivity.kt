package com.yusufkutluay.haydigezeknavigation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.yusufkutluay.haydigezeknavigation.R
import com.yusufkutluay.haydigezeknavigation.databinding.ActivityFragmentFirstBinding

class FragmentFirstActivity : AppCompatActivity() {

    lateinit var binding: ActivityFragmentFirstBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFragmentFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Alt bar rengi değiştirme işlemi
        val window = window
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

        // NavHostFragment'ı ve NavController'ı bulun
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        // BottomNavigationView'i NavController ile kurun
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)

        // Menü öğelerine tıklanıldığında yığını temizle ve ilgili fragment'e git
        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    if (navController.currentDestination?.id == R.id.homeFragment) {
                        navController.navigate(R.id.homeFragment)
                    } else {
                        navController.popBackStack(R.id.homeFragment, false)
                        navController.navigate(R.id.homeFragment)
                    }
                    true
                }
                R.id.shareFragment -> {
                    if (navController.currentDestination?.id == R.id.shareFragment) {
                        navController.navigate(R.id.shareFragment)
                    } else {
                        navController.popBackStack(R.id.shareFragment, false)
                        navController.navigate(R.id.shareFragment)
                    }
                    true
                }
                R.id.listeFragment -> {
                    if (navController.currentDestination?.id == R.id.listeFragment) {
                        navController.navigate(R.id.listeFragment)
                    } else {
                        navController.popBackStack(R.id.listeFragment, false)
                        navController.navigate(R.id.listeFragment)
                    }
                    true
                }
                R.id.profilFragment -> {
                    if (navController.currentDestination?.id == R.id.profilFragment) {
                        navController.navigate(R.id.profilFragment)
                    } else {
                        navController.popBackStack(R.id.profilFragment, false)
                        navController.navigate(R.id.profilFragment)
                    }
                    true
                }
                else -> false
            }
        }
    }
}
