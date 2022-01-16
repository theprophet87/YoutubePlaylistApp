package com.theprophet.youtubeplaylistapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.annotations.SerializedName
import com.theprophet.youtubeplaylistapp.databinding.FragmentEditBinding
import com.theprophet.youtubeplaylistapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

//TODO: Setup JSON functionality to parse Youtube video name from link; do not use in main thread

class EditFragment : Fragment() {
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding
    private var addBtn: Button? = null
    private var ytLink: String? = null

    //this is just for a test
    private var testURL = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=sGqppNOA3QU&format=json"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        val view = binding?.root



        addButton(testURL)



        return view
    }


    fun addButton(url: String){
        //set button
        addBtn = binding?.btnAdd



        addBtn?.setOnClickListener {

            //add youtube link to variable which we can use for JSON parsing
            ytLink = binding?.etLink?.text.toString()
            //Toast.makeText(context, "Video Added: $ytLink",Toast.LENGTH_LONG).show()
           fetchJson(url)



        }

    }



    fun addRecord(playlistDao: PlaylistDao){
        val link = binding?.etLink?.text.toString()


        if(link.isNotEmpty()){
            lifecycleScope.launch {
                playlistDao.insert(PlaylistEntity(link=link)) //set name and email in database
                Toast.makeText(context,"Video Saved", Toast.LENGTH_SHORT).show()

                //clear fields after adding record
                binding?.etLink?.text?.clear()


            }

        }else{
            //if name and email fields are blank, show error message
            Toast.makeText(context,"Name or email cannot be blank."
                , Toast.LENGTH_LONG).show()
        }


    }

    fun fetchJson(url: String){

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback{
            override fun onResponse(call: Call, response: Response) {
                //gets response in String form
                val body = response.body!!.string()

                //creates JSON object with string from response
                val jsonObj:JSONObject = JSONObject(body)

                //parse video title from JSON object
                val VidTitle =jsonObj.getString("title")


                lifecycleScope.launch {
                    Toast.makeText(context,VidTitle,Toast.LENGTH_LONG).show()

                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request")
            }

        })


    }

    data class Video(
        @SerializedName("title")
        val title: String? = null){

    }

}