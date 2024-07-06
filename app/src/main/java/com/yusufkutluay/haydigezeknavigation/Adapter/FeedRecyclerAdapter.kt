package com.yusufkutluay.haydigezeknavigation.Adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.yusufkutluay.haydigezeknavigation.Firebase.FirestoreDatabase
import com.yusufkutluay.haydigezeknavigation.Model.Post
import com.yusufkutluay.haydigezeknavigation.Model.UsersModel
import com.yusufkutluay.haydigezeknavigation.databinding.RecyclerPaylasBinding
import java.text.SimpleDateFormat
import java.util.Locale


class FeedRecyclerAdapter(val postList : ArrayList<Post>) : RecyclerView.Adapter<FeedRecyclerAdapter.PostHolder>() {

    val db = Firebase.firestore
    val database = FirestoreDatabase()
    var listUser = ArrayList<UsersModel>()

    class PostHolder(val binding: RecyclerPaylasBinding) : RecyclerView.ViewHolder(binding.root){


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecyclerPaylasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostHolder(binding)

    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {


        database.getUsers {
            var sayac = 0
            listUser = it
            for (i in listUser){
                if (listUser[sayac].email == postList[position].kullaniciEmail){
                    holder.binding.currentUser.text = listUser[sayac].name
                }
                sayac++
            }
        }


        holder.binding.yorumSatir.setText(postList[position].kullaniciYorum)


        // Timestamp'i String olarak formatlayarak kullanma
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = postList[position].tarih.toDate()
        val formattedDate = sdf.format(date)
        holder.binding.tarih.text = formattedDate


        Picasso.get().load(postList[position].gorselUrl).into(holder.binding.resim)

    }

}