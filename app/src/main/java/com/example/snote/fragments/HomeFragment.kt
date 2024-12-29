package com.example.snote.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.snote.MainActivity
import com.example.snote.R
import com.example.snote.adapter.NoteAdapter
import com.example.snote.databinding.FragmentHomeBinding
import com.example.snote.model.Note
import com.example.snote.viewmodel.NoteViewModel
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment(R.layout.fragment_home), SearchView.OnQueryTextListener, MenuProvider {

    //Declare the binding variable
    private var homeBinding: FragmentHomeBinding? = null
    private val binding get() = homeBinding!!

    //Declare the ViewModel and Adapter variables
    private lateinit var notesViewModel : NoteViewModel
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the Menu
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Initialize the Note ViewModel and RecyclerView
        notesViewModel = (activity as MainActivity).noteViewModel
        setupHomeRecyclerView()

        // When user click on the FAB(Add Note), it will navigate to the AddNoteFragment
        binding.addNoteFab.setOnClickListener {
            it.findNavController().navigate(R.id.action_homeFragment_to_addNoteFragment)
        }

        // When user click on the Upload button, it will upload all the notes to Firebase Firestore
        binding.uploadNotesFab.setOnClickListener {
            Log.d("MyTag", "Upload button clicked")
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("notes").get().addOnSuccessListener { result ->
                for (document in result) {
                    firestore.collection("notes").document(document.id).delete()
                }
                notesViewModel.getAllNotes().observe(viewLifecycleOwner) { notes ->
                    val notesList = notes ?: emptyList()
                    Log.d("MyTag", "Number of notes: ${notesList.size}")
                    for (note in notesList) {
                        Log.d("MyTag", "Sending $note to firebase")
                        notesViewModel.uploadNoteToFirebase(note)
                    }
                    updateUI(notesList)
                }
            }.addOnFailureListener { e ->
                Log.w("MyTag", "Error clearing notes in Firestore", e)
            }
        }

        // When user  click the Download/ Fetch button, it will download all the notes from Firebase Firestore
        binding.downloadNotesFab.setOnClickListener {
            Log.d("MyTag", "Sync button clicked")
            notesViewModel.deleteAllNotes()
            notesViewModel.downloadNotesFromFirebase()
            notesViewModel.getAllNotes().observe(viewLifecycleOwner) { notes ->
                noteAdapter.differ.submitList(notes)
            }
            Log.d("MyTag", "Sync from firebase successful")
        }

    }

    // In case there are no notes, show the empty notes image (res/drawable/empty_sticky_notes.png), else show the RecyclerView
    private fun updateUI(notes: List<Note>?) {
        if (notes != null) {
            if (notes.isNotEmpty()) {
                binding.emptyNotesImage.visibility = View.GONE
                binding.homeRecyclerView.visibility = View.VISIBLE
            } else {
                binding.emptyNotesImage.visibility = View.VISIBLE
                binding.homeRecyclerView.visibility = View.GONE
            }
        }
    }

    // Setup the RecyclerView with the NoteAdapter
    private fun setupHomeRecyclerView(){
        noteAdapter = NoteAdapter()
        binding.homeRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            adapter = noteAdapter
        }
        // GridLayoutManager is used to display the notes in a grid layout with 2 columns, as in the
        // UI we seen there are 2 note items in a row.

        activity?.let {
            notesViewModel.getAllNotes().observe(viewLifecycleOwner){ note ->
                noteAdapter.differ.submitList(note)
                updateUI(note) // If notes are empty, show the empty notes image.
            }
            // Observe the LiveData getAllNotes() from the ViewModel, and submit the list of notes to the adapter.
        }
    }

    private fun searchNote(query: String?){
        val searchQuery = "%$query"

        notesViewModel.searchNote(searchQuery).observe(this) {list ->
            noteAdapter.differ.submitList(list)
        }
    }

    // Implement functions for the SearchView
    // We want when user still typing, the search result will start to show.
    // Not when user press enter, then give the search result.
    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null){
            searchNote(newText)
        }
        return true
    }

    // Fragment no longer in used
    override fun onDestroy() {
        super.onDestroy()
        homeBinding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.home_menu, menu)

        val menuSearch = menu.findItem(R.id.searchMenu).actionView as SearchView
        menuSearch.isSubmitButtonEnabled = false
        menuSearch.setOnQueryTextListener(this)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }
}