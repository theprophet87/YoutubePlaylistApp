package com.theprophet.youtubeplaylistapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.theprophet.youtubeplaylistapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

//TODO: 1. Make playlist entries to play youtube video

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding
    private var plDao: PlaylistDao? = null


    private var list: RecyclerView? = null
    private var youTubePlayerView: YouTubePlayerView? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding?.root
        youTubePlayerView = binding?.ytPlayerView

        initYoutubePlayerView()

        //instantiate database
        val db = PlaylistDatabase.getInstance(requireContext())
        plDao = db.playlistDao()

        //display Recyclerview
        lifecycleScope.launch {
            plDao!!.fetchAllLinks().collect{
                val list = ArrayList(it)

                setupListOfDataIntoRecyclerView(list, plDao!!)


            }

        }








        return view
    }



    override fun onDestroy() {
        super.onDestroy()
        youTubePlayerView?.release()
        _binding = null
    }

    private fun initYoutubePlayerView(){
        list = _binding?.recyclerViewProjects

        lifecycle.addObserver(youTubePlayerView!!)

        youTubePlayerView!!.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {

                youTubePlayer.loadVideo("sGqppNOA3QU", 0f)
            }
        })

    }

    //method to show RecyclerView

    private fun setupListOfDataIntoRecyclerView(playlistList: ArrayList<PlaylistEntity>,
                                                playlistDao: PlaylistDao){

        if(playlistList.isNotEmpty()){

            val homeAdapter = HomeAdapter(playlistList)

            binding?.recyclerViewProjects?.layoutManager = LinearLayoutManager(context)
            binding?.recyclerViewProjects?.adapter = homeAdapter

            //The itemClick listener logic to play youtube videos
            homeAdapter.setOnItemClickListener(object : HomeAdapter.onItemClickListener {
                override fun onItemClick(position: Int) {
                    Toast.makeText(context, "You clicked on item no. $position", Toast.LENGTH_LONG).show()
                }


            })
            binding?.recyclerViewProjects?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE


        }else{
            binding?.recyclerViewProjects?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE

        }

    }


}