package com.theprophet.youtubeplaylistapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.beust.klaxon.Klaxon
import com.theprophet.youtubeplaylistapp.databinding.FragmentEditBinding
import com.theprophet.youtubeplaylistapp.databinding.DialogUpdateBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.concurrent.ArrayBlockingQueue

//TODO: 1. implement exception handling 2. Fix recycler view delete issues with visuals

class EditFragment : Fragment()  {
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding
    private var ytLink: String? = null
    private var mTitle: String? = null
    private var mAuthorName: String? = null
    private var plDao: PlaylistDao? = null
    private val client = OkHttpClient()
    private val blockingQueue: ArrayBlockingQueue<JSONObject> =
        ArrayBlockingQueue(1)
    private var jsonContact: JSONObject? = null






    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        val view = binding?.root
        //create instance of database
        val db = PlaylistDatabase.getInstance(requireContext())
        plDao = db.playlistDao()



        //logic for when Add button is clicked
        binding?.btnAdd?.setOnClickListener {

            /* load user input (youtube link) into variable as String */
            ytLink = binding?.etLink?.text.toString()

            /* send request to get JSON object and store in
            * blocking queue */

            fetch(ytLink!!)

            /* assign element in blocking queue to variable to work with */
            try {
                jsonContact = blockingQueue.take()

            }catch(e: InterruptedException){
                e.printStackTrace()
            }

            mTitle = jsonContact!!.getString("title")
            mAuthorName = jsonContact!!.getString("author_name")

            Log.d("message","title: $mTitle")

           //add data into record
            addRecord(plDao!!,mTitle!!,mAuthorName!!, ytLink!!)

        }

        return view
    }

    //fetches json object from Youtube
    private fun fetch(sUrl: String){
        val client = OkHttpClient()
        // Create URL
        //concat the original url with format for youtube JSON object
        val finalUrl = "https://www.youtube.com/oembed?url=$sUrl&format=json"
        val url = URL(finalUrl)

        // Build request
        val request = Request.Builder().
        url(url).
        build()
        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call, response: Response) {
                val json_contact = response.body!!.string()
                val responseJSON = JSONObject(json_contact)

                //add json object to queue to be parsed later
               blockingQueue.add(responseJSON)

            }


        })
    }


    private fun addRecord(playlistDao: PlaylistDao,
                          title: String, author: String, link: String){


        if(binding?.etLink?.text!!.isNotEmpty()){
            lifecycleScope.launch {
               playlistDao.insert(PlaylistEntity(title=title, author=author, link = link)) //set vid title and author in database
                Toast.makeText(context,"Video Saved: $author: $title", Toast.LENGTH_SHORT).show()

                //clear fields after adding record
                binding?.etLink?.text?.clear()



                //add to Recyclerview
                lifecycleScope.launch {
                playlistDao.fetchAllLinks().collect{
                    val list = ArrayList(it)

                    setupListOfDataIntoRecyclerView(list, playlistDao)


                     }

                }

            }

        }else{
            //if link field is blank, show error message
            Toast.makeText(context,"Please enter a link."
                , Toast.LENGTH_LONG).show()
        }


    }

    private fun setupListOfDataIntoRecyclerView(playlistList: ArrayList<PlaylistEntity>,
                                                 playlistDao: PlaylistDao){

        if(playlistList.isNotEmpty()){

            val itemAdapter = ItemAdapter(playlistList,
                {
                        updateId ->
                    updateRecordDialog(updateId, playlistDao)
                },
                {
                        deleteId ->
                    deleteRecordAlertDialog(deleteId, playlistDao)
                }

            )

            binding?.rvItemsList?.layoutManager = LinearLayoutManager(context)
            binding?.rvItemsList?.adapter = itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE


        }

    }

    private fun deleteRecordAlertDialog(id: Int, playlistDao: PlaylistDao) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Record")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes"){
                dialogInterface, _->
            lifecycleScope.launch {
                playlistDao.delete(PlaylistEntity(id))
                Toast.makeText(context, "Video deleted successfully.",
                    Toast.LENGTH_LONG).show()

            }
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("No"){
                dialogInterface, _->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.show()

    }



    private fun updateRecordDialog(id: Int, playlistDao: PlaylistDao){
        val updateDialog= Dialog(requireContext(), R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch {
            playlistDao.fetchLinkById(id).collect {
                if(it != null){
                    binding.etUpdateLink.setText(it.link)


                }


            }
        }
        binding.tvUpdate.setOnClickListener {
            //updated link from user
            val link = binding.etUpdateLink.text.toString()



            if(link.isNotEmpty()){

                fetch(link)

                /* assign element in blocking queue to variable to work with */
                try {
                    jsonContact = blockingQueue.take()

                }catch(e: InterruptedException){
                    e.printStackTrace()
                }

                val title = jsonContact!!.getString("title")
                val author = jsonContact!!.getString("author_name")

                Log.d("message", "title: $title author: $author")


                lifecycleScope.launch {
                    playlistDao.update(PlaylistEntity(id, link = link, title = title, author = author ))
                    Toast.makeText(context, "Record Updated",Toast.LENGTH_LONG).show()
                    updateDialog.dismiss()


                }


            }else{

                Toast.makeText(context, "Link cannot be blank.",Toast.LENGTH_LONG).show()
                updateDialog.dismiss()
            }
        }

        binding.tvCancel.setOnClickListener {
            updateDialog.dismiss()

        }

        updateDialog.show()

    }


    fun makeJSON(s: String?): JSONObject {
        try {

            return  JSONObject(s!!)

        }catch(e: JSONException){


            Toast.makeText(context, "Please paste valid Youtube link.", Toast.LENGTH_LONG).show()

            null
        }
        return JSONObject(s!!)
    }

}