package com.yusufkutluay.haydigezeknavigation.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.yusufkutluay.haydigezeknavigation.AnaSayfaMenu.HomeFragmentDirections
import com.yusufkutluay.haydigezeknavigation.Model.SehirModel
import com.yusufkutluay.haydigezeknavigation.databinding.RecyclerSehirBinding

class RecyclerSehirler(val sehirList : ArrayList<SehirModel>) : RecyclerView.Adapter<RecyclerSehirler.SehirHolder>() {

    class SehirHolder(val binding: RecyclerSehirBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SehirHolder {
        val binding = RecyclerSehirBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SehirHolder(binding)
    }

    override fun getItemCount(): Int {
        return sehirList.size
    }

    override fun onBindViewHolder(holder: SehirHolder, position: Int) {


        holder.binding.sehirName.text = sehirList[position].name
        Picasso.get().load(sehirList[position].url).into(holder.binding.sehirUrl)

        holder.binding.cardBas.setOnClickListener {

            val navController = holder.itemView.findNavController()
            val action = HomeFragmentDirections.actionHomeFragmentToSehirFragment(sehirList[position].name)
            navController.navigate(action)

        }




    }




}