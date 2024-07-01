package com.yusufkutluay.haydigezeknavigation.AlertDiaolog


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

class Uyari {

    //ethernet kontrol

    fun kontrolEtEthernet(context: Context?) {
        if (!context?.let { isInternetAvailable(it) }!!) {
            showNoInternetDialog(context)
        }
    }


    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun showNoInternetDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Uyarı")
        builder.setMessage("İnternet bağlantınız yok. Lütfen bağlantınızı kontrol edin.")
        builder.setPositiveButton("Etkinleştir", DialogInterface.OnClickListener { dialog, which ->
            context.startActivity(Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS))
            //ACTION_NETWORK_OPERATOR_SETTINGS
        })
        builder.setNegativeButton("İptal",DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        builder.setCancelable(false)  // Diyalog ekrana dokunarak veya geri tuşuna basarak kapatılamaz
        builder.show()
    }
}