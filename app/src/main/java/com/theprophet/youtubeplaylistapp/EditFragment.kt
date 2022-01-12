package com.theprophet.youtubeplaylistapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.theprophet.youtubeplaylistapp.databinding.FragmentEditBinding
import com.theprophet.youtubeplaylistapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch


class EditFragment : Fragment() {
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding
    private var addBtn: Button? = null
    private var ytLink: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        val view = binding?.root






        return view
    }


    fun addButton(){
        //set button
        addBtn = binding?.btnAdd

        addBtn?.setOnClickListener {

            //add youtube link to variable which we can use for JSON parsing
            ytLink = binding?.etLink?.text.toString()



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

}