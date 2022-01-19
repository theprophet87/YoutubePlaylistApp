package com.theprophet.youtubeplaylistapp

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.theprophet.youtubeplaylistapp.databinding.FragmentEditBinding
import com.theprophet.youtubeplaylistapp.databinding.DialogUpdateBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

//TODO: We are now able to add items to database!!
//TODO: We can start adding items to adapter class to show in RecyclerView

class EditFragment : Fragment() {
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding
    private var ytLink: String? = null
    private var jsonContact: JSONObject? = null
    private var jsonObj:JSONObject? = null

    //TODO: set up adapter to show entries

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        val view = binding?.root

        //create instance of database
        val db = PlaylistDatabase.getInstance(context!!)
        val plDao = db.playlistDao()


        //logic for when Add button is clicked
        binding?.btnAdd?.setOnClickListener {

            /* load user input (youtube link) into variable as String */

           ytLink = binding?.etLink?.text.toString()

            /* fetch JSON object */

            //concat the original url with format for youtube JSON object
            val finalUrl = "https://www.youtube.com/oembed?url=$ytLink&format=json"


            //use OKHttp to make request to youtube and receive response, which we use to create JSON obj
            val request = Request.Builder().url(finalUrl).build()
            val client = OkHttpClient()


            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    /* We can do the adding and updating if we get a response */

                    //gets response in String form
                    val str_response = response.body!!.string()

                    //creates JSON object with string from response
                    jsonContact = JSONObject(str_response)

                    //local variables
                    var title = jsonContact?.getString("title")
                    var author = jsonContact?.getString("author_name")

                    addRecord(plDao, title!!, author!!, ytLink!!)

                }

                override fun onFailure(call: Call, e: IOException) {
                    println("Failed to execute request")
                }

            })


        }


        /*
        lifecycleScope.launch {
            plDao.fetchAllLinks().collect {
                val list = ArrayList(it)

                setupListOfDataIntoRecyclerView(list, plDao)
            }
        }

        */


        return view
    }


    private fun addRecord(playlistDao: PlaylistDao,
                          title: String, author: String, link: String){


        if(binding?.etLink?.text!!.isNotEmpty()){
            lifecycleScope.launch {
                playlistDao.insert(PlaylistEntity(title=title, author=author, link = link)) //set vid title and author in database
                Toast.makeText(context,"Video Saved", Toast.LENGTH_SHORT).show()

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
        val updateDialog= Dialog(context!!, R.style.Theme_Dialog)
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

            //create new JSON obj based on updated link
           // val newJson = fetchJson(link)

            //update title and author
            //val title = newJson!!.getString("title")
            //val author = newJson.getString("author")

            if(link.isNotEmpty()){
                lifecycleScope.launch {
                   // playlistDao.update(PlaylistEntity(id, link = link, title = title, author = author ))
                   // Toast.makeText(context, "Record Updated",Toast.LENGTH_LONG).show()
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

}