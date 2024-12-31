package com.example.snote.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.snote.model.Note

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)
    // onConflict = OnConflictStrategy.REPLACE is used to replace the existing note with the new one if the note with the same id already exists.
    // suspend keyword indicates that the function is called from coroutine. It runs in a background thread.

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)


    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE noteTitle LIKE :query OR noteDesc LIKE :query")
    fun searchNote(query: String?): LiveData<List<Note>>
    // Query no need to be suspend because since it already run on a special background thread, specifically designed for Room database operations.
}