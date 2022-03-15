package com.theprophet.youtubeplaylistapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.theprophet.youtubeplaylistapp.databinding.ItemRowBinding
import com.theprophet.youtubeplaylistapp.databinding.PlayItemsListBinding

class HomeAdapter (private val items: ArrayList<PlaylistEntity>
): RecyclerView.Adapter<HomeAdapter.ViewHolder>(){
    class ViewHolder(binding: PlayItemsListBinding): RecyclerView.ViewHolder(binding.root){

        val llMain = binding.llHome
        val tvName = binding.tvName
        val tvAuthor = binding.tvAuthor
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.ViewHolder {
        return HomeAdapter.ViewHolder(PlayItemsListBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: HomeAdapter.ViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = items[position]

        holder.tvName.text = item.title
        holder.tvAuthor.text = item.author


    }

    override fun getItemCount(): Int {

        return items.size
    }

}