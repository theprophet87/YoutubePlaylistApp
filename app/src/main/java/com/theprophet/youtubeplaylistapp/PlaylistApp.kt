package com.theprophet.youtubeplaylistapp

import android.app.Application

class PlaylistApp: Application() {

    val db by lazy{
        PlaylistDatabase.getInstance(this)

    }
}