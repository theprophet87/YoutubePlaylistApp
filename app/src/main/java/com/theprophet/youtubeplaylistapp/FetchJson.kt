package com.theprophet.youtubeplaylistapp

import android.content.Context
import android.widget.Toast
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL


class FetchJson: Callback {
    /* member variables */
    private var title: String? = null
    private var author_name: String? = null
    private var onRequestCompleteListener: OnRequestCompleteListener? = null
    private var responseContainer: JSONObject? = null

    //create Okttp client
    private val client = OkHttpClient()

    /* setters */

    fun setTitle(t: String){
        title = t
    }

    fun setAuthor(a: String){
        author_name = a
    }

    /* getters */
    fun getTitle(): String?{
        return title

    }

    fun getAuthorName(): String?{
        return author_name

    }

    fun fetch(callback: OnRequestCompleteListener, sUrl: String){
        this.onRequestCompleteListener = callback
        val client = OkHttpClient()
        // Create URL
        val url = URL(sUrl)

        // Build request
        val request = Request.Builder().
        url(url).
        build()
        client.newCall(request).enqueue(this)


    }

    override fun onFailure(call: Call, e: IOException) {
        print("error")
    }

    override fun onResponse(call: Call, response: Response) {

        if(response.isSuccessful) {
            val body = response.body?.string()
            responseContainer = JSONObject(body!!)

            parse(responseContainer!!)
        }
        onRequestCompleteListener?.onSuccess(responseContainer!!)
    }

    private fun parse(r: JSONObject){
        this.responseContainer = r

    }
}

interface OnRequestCompleteListener{
    fun onSuccess(response: JSONObject){

    }

}