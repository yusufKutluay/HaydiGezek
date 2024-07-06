package com.yusufkutluay.haydigezeknavigation.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.yusufkutluay.haydigezeknavigation.Model.SeyahatModel
import com.yusufkutluay.haydigezeknavigation.databinding.RecyclerSeyahatBinding
import java.text.SimpleDateFormat
import java.util.Locale

class RecyclerSeyahat(val seyahatList : ArrayList<SeyahatModel>) : RecyclerView.Adapter<RecyclerSeyahat.SeyahatHolder>() {

    private lateinit var db  : FirebaseFirestore
    private lateinit var auth : FirebaseAuth

    class SeyahatHolder(val binding: RecyclerSeyahatBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeyahatHolder {
        val binding = RecyclerSeyahatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeyahatHolder(binding)
    }

    override fun getItemCount(): Int {
        return seyahatList.size
    }

    override fun onBindViewHolder(holder: SeyahatHolder, position: Int) {
        auth = FirebaseAuth.getInstance()

        holder.binding.seyahatName.text = seyahatList[position].name

        // Timestamp'i String olarak formatlayarak kullanma
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = seyahatList[position].tarih.toDate()
        val formattedDate = sdf.format(date)
        holder.binding.seyahatTarih.text = formattedDate

        Picasso.get().load(seyahatList[position].url).into(holder.binding.seyahatUrl)



    }




}