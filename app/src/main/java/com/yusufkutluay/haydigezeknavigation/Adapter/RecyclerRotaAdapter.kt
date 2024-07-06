package com.yusufkutluay.haydigezeknavigation.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.yusufkutluay.haydigezeknavigation.AnaSayfaMenu.HomeFragmentDirections
import com.yusufkutluay.haydigezeknavigation.Model.RotaModel
import com.yusufkutluay.haydigezeknavigation.databinding.RecyclerRotaBinding

class RecyclerRotaAdapter(val rotaList: ArrayList<RotaModel>) : RecyclerView.Adapter<RecyclerRotaAdapter.PlaceRotaHolder>() {

    class PlaceRotaHolder(val binding : RecyclerRotaBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceRotaHolder {
        val binding = RecyclerRotaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceRotaHolder(binding)
    }

    override fun getItemCount(): Int {
        return rotaList.size
    }

    override fun onBindViewHolder(holder: PlaceRotaHolder, position: Int) {

        val rotaPlace = rotaList[position]

        holder.binding.textView5.text = rotaPlace.name

        holder.binding.imageView15.setOnClickListener {

            val navController = holder.itemView.findNavController() // Görünümdən NavController'i al

            // Tıklanan RotaModel'in ismini argüman olarak kullanarak BilgiFragment'e git
            val action = HomeFragmentDirections.actionHomeFragmentToBilgiFragment(rotaPlace.name,"HomeFragment")
            navController.navigate(action)

        }

        // picasso yöntemiyle url yi ImageView a aktarma
        Picasso.get().load(rotaList[position].url).into(holder.binding.imageView15)


    }
}