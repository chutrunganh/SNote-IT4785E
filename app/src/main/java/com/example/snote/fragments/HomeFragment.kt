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

    private var homeBinding: FragmentHomeBinding? = null
    private val binding get() = homeBinding!!

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

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        notesViewModel = (activity as MainActivity).noteViewModel
        setupHomeRecyclerView()

        binding.addNoteFab.setOnClickListener {
            it.findNavController().navigate(R.id.action_homeFragment_to_addNoteFragment)
        }

        binding.syncButton.setOnClickListener {
            Log.d("MyTag", "Sync button clicked")
            notesViewModel.fetchNotesFromFirebase()
            notesViewModel.getAllNotes().observe(viewLifecycleOwner) { notes ->
                noteAdapter.differ.submitList(notes)
                updateUI(notes)
            }
            Log.d("MyTag", "Sync from firebase successful")
        }


//        // Observe the LiveData from the ViewModel to update the UI when the database changes
//        notesViewModel.getAllNotes().observe(viewLifecycleOwner) { notes ->
//            noteAdapter.differ.submitList(notes)
//            updateUI(notes)
//        }

        binding.uploadButton.setOnClickListener {
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
                        notesViewModel.saveNoteToFirebase(note)
                    }
                }
            }.addOnFailureListener { e ->
                Log.w("MyTag", "Error clearing notes in Firestore", e)
            }
        }



    }

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

    private fun setupHomeRecyclerView(){
        noteAdapter = NoteAdapter()
        binding.homeRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            adapter = noteAdapter
        }

        activity?.let {
            notesViewModel.getAllNotes().observe(viewLifecycleOwner){ note ->
                noteAdapter.differ.submitList(note)
                updateUI(note)
            }
        }
    }

    private fun searchNote(query: String?){
        val searchQuery = "%$query"

        notesViewModel.searchNote(searchQuery).observe(this) {list ->
            noteAdapter.differ.submitList(list)
        }
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null){
            searchNote(newText)
        }
        return true
    }

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