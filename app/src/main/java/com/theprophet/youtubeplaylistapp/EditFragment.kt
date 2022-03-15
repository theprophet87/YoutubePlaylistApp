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
import androidx.lifecycle.coroutineScope
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
import java.lang.Error
import java.lang.IllegalStateException

import java.net.URL
import java.util.concurrent.ArrayBlockingQueue
import kotlin.NullPointerException


class EditFragment : Fragment()  {
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding
    private var ytLink: String? = null
    private var mTitle: String? = null
    private var mAuthorName: String? = null
    private var plDao: PlaylistDao? = null

    //This will hold the data from the API response
    private val StringBlockingQueue: ArrayBlockingQueue<String> =
        ArrayBlockingQueue(1)

    //flag to check if an object is JSONobject or not
    private var isJson: Boolean? = false
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


        //display Recyclerview
        lifecycleScope.launch {
            plDao!!.fetchAllLinks().collect{
                val list = ArrayList(it)

                setupListOfDataIntoRecyclerView(list, plDao!!)


            }

        }

        //logic for when Add button is clicked
        binding?.btnAdd?.setOnClickListener {

            /* load user input (youtube link) into variable as String */
            ytLink = binding?.etLink?.text.toString()

            /* send request to get OKhttp string response and store in
            * blocking queue */

            if (binding?.etLink?.text!!.isNotEmpty()) {
                fetch(ytLink!!)

                //make string a JSON object
                val responseString = StringBlockingQueue.take()
                try{
                    jsonContact =  makeJSON(responseString)
                    isJson = true
                }catch(e: JSONException){
                    lifecycleScope.launch {
                        Toast.makeText(context, "Please enter a valid Youtube link.",
                            Toast.LENGTH_SHORT).show()
                        isJson = false
                    }
                }

                if(isJson == true){
                    /* assign element in blocking queue to variable to work with */
                    /* set title and author variables */
                    mTitle = jsonContact!!.getString("title")
                    mAuthorName = jsonContact!!.getString("author_name")

                    try {
                        //add data into record
                        addRecord(plDao!!, mTitle!!, mAuthorName!!, ytLink!!)
                    }catch(e: NullPointerException){
                        //if link is invalid
                        lifecycleScope.launch {
                            Toast.makeText(context, "Please enter a link.",
                                Toast.LENGTH_SHORT).show()

                        }
                    }
                }

                } else {

                    //if link field is blank, show error message
                    lifecycleScope.launch {
                        Toast.makeText(context, "Please enter a link.",
                            Toast.LENGTH_SHORT).show()

                    }

                }

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
                Log.e("message", "Error")
            }

            override fun onResponse(call: Call, response: Response) {

                //receive response in string form from Youtube API
                val responseString = response.body!!.string()

                //add string to blocking queue
                StringBlockingQueue.add(responseString)

                }

        })
    }

    private fun makeJSON(s: String): JSONObject{

                    return JSONObject(s)


    }

    private fun addRecord(playlistDao: PlaylistDao,
                          title: String, author: String, link: String){


        if(binding?.etLink?.text!!.isNotEmpty()){
            lifecycleScope.launch {
               playlistDao.insert(PlaylistEntity(title=title, author=author, link = link)) //set vid title and author in database
                Toast.makeText(context,"Video Saved: $author: $title", Toast.LENGTH_SHORT).show()

                //clear fields after adding record
                binding?.etLink?.text?.clear()





            }

        }else{
            //if link field is blank, show error message
            Toast.makeText(context,"Please enter a link."
                , Toast.LENGTH_LONG).show()
        }


    }

    private fun setupListOfDataIntoRecyclerView(playlistList: ArrayList<PlaylistEntity>,
                                                 playlistDao: PlaylistDao){

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
        if(playlistList.isNotEmpty()){

            binding?.rvItemsList?.layoutManager = LinearLayoutManager(context)
            binding?.rvItemsList?.adapter = itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE


        }else{
            binding?.rvItemsList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE

        }

    }

    private fun deleteRecordAlertDialog(id: Int, playlistDao: PlaylistDao) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Record")


        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete this video?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes"){
                dialogInterface, _->
            lifecycleScope.launch {
                playlistDao.delete(PlaylistEntity(id))
                Toast.makeText(context, "Video deleted successfully.",
                    Toast.LENGTH_LONG).show()
                dialogInterface.dismiss()
            }

        }
        builder.setNegativeButton("No"){
                dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()

        alertDialog.setCancelable(false) // will not allow user to cancel after clicking on remaining screen area
        alertDialog.show() //show dialog in UI

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

                /* add and retrieve OKhttp response string from blocking queue */

                    // get string from blocking queue
                    val response = StringBlockingQueue.take()

                    try{
                        //make string into JSON object
                       jsonContact = makeJSON(response)

                    }catch(e: JSONException){
                        //if link is invalid, show error message
                        lifecycleScope.launch {
                            Toast.makeText(context, "Please enter a valid link.",
                                Toast.LENGTH_LONG).show()
                        }
                    }

                //set variables with title and author name
                val title = jsonContact!!.getString("title")
                val author = jsonContact!!.getString("author_name")

                //update database
                lifecycleScope.launch {
                    playlistDao.update(PlaylistEntity(id, link = link, title = title, author = author ))
                    Toast.makeText(context, "Record Updated",Toast.LENGTH_SHORT).show()
                    updateDialog.dismiss()

                }

            }else{
                //if link is blank, show error message
                Toast.makeText(context, "Link cannot be blank.",Toast.LENGTH_LONG).show()
                updateDialog.dismiss()
            }
        }

        binding.tvCancel.setOnClickListener {
            updateDialog.dismiss()

        }

        updateDialog.show()

        }

    }

