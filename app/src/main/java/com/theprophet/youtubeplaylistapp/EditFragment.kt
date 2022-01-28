package com.theprophet.youtubeplaylistapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.beust.klaxon.Klaxon
import com.theprophet.youtubeplaylistapp.databinding.FragmentEditBinding
import com.theprophet.youtubeplaylistapp.databinding.DialogUpdateBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import androidx.lifecycle.Observer
import com.beust.klaxon.KlaxonException
import okhttp3.*
import okio.IOException
import org.json.JSONObject
import java.net.URL

//TODO: Add valid youtube link data to database

class EditFragment : Fragment() {
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding
    private var ytLink: String? = null


    // Create OkHttp Client
    private val client = OkHttpClient()

    //create viewModel
    val viewModel: MainActivityViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        val view = binding?.root

        //create instance of database
        val db = PlaylistDatabase.getInstance(requireContext())
        val plDao = db.playlistDao()


        //logic for when Add button is clicked
        binding?.btnAdd?.setOnClickListener {

            /* load user input (youtube link) into variable as String */

           ytLink = binding?.etLink?.text.toString()

            /* fetch JSON object */

            //concat the original url with format for youtube JSON object
            val finalUrl = "https://www.youtube.com/oembed?url=$ytLink&format=json"

            fetch(finalUrl)

            /* use 'fetch' method to get response from YT site and parse JSON object
            using Klaxon with string from response
             */

            //local variables
           // val title = jsonContact.getString("title")
           // val author = jsonContact.getString("author_name")

          //  addRecord(plDao, title!!, author!!, ytLink!!)


            lifecycleScope.launch {


                /*
                plDao.fetchAllLinks().collect {
                    val list = ArrayList(it)

                    setupListOfDataIntoRecyclerView(list, plDao)
                }

                 */
            }


        }








        return view
    }

    private fun fetch(sUrl: String): VideoInfo?{
        var vidInfo: VideoInfo? = null

        //if the input field is blank, we should display an error message
        if(binding?.etLink?.text!!.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val result = getRequest(sUrl)

                when {
                    result != null -> {



                           try {


                               // Parse result string JSON to data class
                               vidInfo = Klaxon().parse<VideoInfo>(result)




                               withContext(Dispatchers.Main) {
                                   // Update view model
                                   viewModel.title.value = vidInfo?.title
                                   viewModel.author_name.value = vidInfo?.author_name

                                   Toast.makeText(
                                       context,
                                       "${viewModel.title.value}",
                                       Toast.LENGTH_LONG
                                   )
                                       .show()


                               }


                           } catch (e: KlaxonException) {

                               //if input is invalid
                               lifecycleScope.launch {
                                   Toast.makeText(context, "Please paste valid Youtube link.", Toast.LENGTH_LONG).show()

                               }

                           }


                    }

                    else -> {

                        print("Error: Get request returned no response")
                    }
                }

            }
        }else{
            //error message if input field is empty
            Toast.makeText(context, "Please paste valid Youtube link.", Toast.LENGTH_LONG).show()
        }
        return vidInfo
    }

    private fun getRequest(sUrl: String): String?{
        var result: String? = null

        try {
            // Create URL
            val url = URL(sUrl)

            // Build request
            val request = Request.Builder().url(url).build()

            // Execute request
            val response = client.newCall(request).execute()
            response.body?.string().also { result = it }



        }catch (err: Error){

            print("Error when executing get request: "+err.localizedMessage)
        }

        return result
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

            //create new JSON obj based on updated link
           // val newJson = fetchJson(link)

            //update title and author


            /*

           val title = jsonContact?.getString("title")
            val author = jsonContact?.getString("author")

             */



            if(link.isNotEmpty()){

                /*
                lifecycleScope.launch {
                  playlistDao.update(PlaylistEntity(id, link = link, title = title!!, author = author!! ))
                   Toast.makeText(context, "Record Updated",Toast.LENGTH_LONG).show()
                    updateDialog.dismiss()


                }

                 */

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