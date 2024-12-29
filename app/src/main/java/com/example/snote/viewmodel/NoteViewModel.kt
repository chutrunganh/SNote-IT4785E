package com.example.snote.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.snote.model.Note
import com.example.snote.repository.NoteRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.text.get


class NoteViewModel(app: Application, private val noteRepository: NoteRepository): AndroidViewModel(app) {

    fun addNote(note: Note) =
        viewModelScope.launch {
            noteRepository.insertNote(note)
        }

    fun deleteNote(note: Note) =
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }

    fun updateNote(note: Note) =
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }

    fun getAllNotes() = noteRepository.getAllNotes()

    fun searchNote(query: String?) = noteRepository.searchNote(query)


    fun fetchNotesFromFirebase() {
        val firestore = FirebaseFirestore.getInstance()
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("notes").get().await()
                val notes = snapshot.documents.map { document ->
                    Note(
                        id = document.getLong("id")?.toInt() ?: 0,
                        noteTitle = document.getString("title") ?: "",
                        noteDesc = document.getString("content") ?: ""
                    )
                }
                notes.forEach { note ->
                    noteRepository.insertNote(note)
                }
                Log.d("MyTag", "Notes fetched and updated in local database successfully")
            } catch (e: Exception) {
                Log.w("MyTag", "Error fetching notes from Firestore", e)
            }
        }
    }

    fun saveNoteToFirebase(note: Note) {
        val firestore = FirebaseFirestore.getInstance()
        Log.i("MyTag", "Receive single note from home fragment: $note, sending to Firestore")
        val noteMap = hashMapOf(
            "id" to note.id,
            "title" to note.noteTitle,
            "content" to note.noteDesc
        )
        Log.d("MyTag", "Attempting to save note to Firestore: $noteMap")
        firestore.collection("notes").document(note.id.toString()).set(noteMap)
            .addOnSuccessListener {
                Log.d("MyTag", "Note added/updated in Firestore successfully")
            }
            .addOnFailureListener { e ->
                Log.w("MyTag", "Error adding/updating note in Firestore", e)
            }
    }
}