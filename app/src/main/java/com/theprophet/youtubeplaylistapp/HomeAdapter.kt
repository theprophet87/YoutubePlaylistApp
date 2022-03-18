package com.theprophet.youtubeplaylistapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.theprophet.youtubeplaylistapp.databinding.PlayItemsListBinding

class HomeAdapter(
    private val items: ArrayList<PlaylistEntity>,
): RecyclerView.Adapter<HomeAdapter.ViewHolder>(){
    private val pos: Int? = null
    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    class ViewHolder(binding: PlayItemsListBinding, listener: onItemClickListener): RecyclerView.ViewHolder(binding.root){


        val tvName = binding.tvName
        val tvAuthor = binding.tvAuthor

        //kotlin version of constructor
        init {
            itemView.setOnClickListener {
                listener.onItemClick(bindingAdapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.ViewHolder {
        return HomeAdapter.ViewHolder(PlayItemsListBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false),mListener)
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



    fun setOnItemClickListener(listener: HomeAdapter.onItemClickListener){
        mListener = listener
    }
}

