package com.theprophet.youtubeplaylistapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.theprophet.youtubeplaylistapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding


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
}