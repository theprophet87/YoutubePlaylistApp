package com.theprophet.youtubeplaylistapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist-table")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var link: String ="",
    var title: String = "",
    //@ColumnInfo(name = "author-id")
    var author: String = ""
)
