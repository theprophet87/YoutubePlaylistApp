package com.theprophet.youtubeplaylistapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert
    suspend fun insert(playlistEntity: PlaylistEntity)

    @Update
    suspend fun update(playlistEntity: PlaylistEntity)

    @Delete
    suspend fun delete(playlistEntity: PlaylistEntity)

    @Query("Select * from `playlist-table`")
    fun fetchAllLinks(): Flow<List<PlaylistEntity>>

    @Query("Select * from `playlist-table` where id=:id")
    fun fetchLinkById(id:Int): Flow<PlaylistEntity>

}