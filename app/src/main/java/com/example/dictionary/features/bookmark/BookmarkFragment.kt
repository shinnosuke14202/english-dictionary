package com.example.dictionary.features.bookmark

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.dictionary.R
import com.example.dictionary.data.local.AppDatabase
import com.example.dictionary.data.local.WordRepository
import com.example.dictionary.databinding.FragmentBookmarkBinding
import com.example.dictionary.features.detail.DetailFragment
import com.example.dictionary.features.word.Word
import com.example.dictionary.features.word.WordViewModel
import com.example.dictionary.features.word.WordViewModelFactory
import com.example.dictionary.network.DictionarySite
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class BookmarkFragment : Fragment() {

    private lateinit var binding: FragmentBookmarkBinding
    private lateinit var viewModel: WordViewModel

    private var checkedItems = mutableListOf<Word>()

    private val db by lazy {
        AppDatabase.getInstance(requireContext())
    }
    private val dictionarySite = DictionarySite()
    private val wordRepository by lazy {
        WordRepository(db.wordDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this, WordViewModelFactory(dictionarySite, wordRepository)
        )[WordViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookmarkBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        binding.toolbar.menu.findItem(R.id.itemUnbookmark).setOnMenuItemClickListener {
            if (checkedItems.isNotEmpty()) {
                showConfirmDialog()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Select at least 1 item to remove",
                    Toast.LENGTH_SHORT
                ).show()
            }
            true
        }
    }

    private fun showConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Removal")
            .setMessage("Are you sure you want to remove the bookmark(s) for these items?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteBookmarkedItems()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteBookmarkedItems() {
        checkedItems.forEach {
            viewModel.deleteWord(word = it)
        }
        checkedItems.clear()
    }

    private fun setupRecyclerView() {
        viewModel.getAllWords()
        lifecycleScope.launch {
            viewModel.allWords.collect {
                binding.rv.adapter = BookmarkRVAdapter(
                    onClick = { word ->
                        navigateToDetail(word)
                    },
                    items = it,
                    getCheckedItems = { i ->
                        checkedItems = i.toMutableList()
                    },
                )
            }
        }
    }

    private fun navigateToDetail(word: Word) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack(null)
            .replace(R.id.clBookmark, DetailFragment.newInstance(word)).commit()
    }

    companion object {
        @JvmStatic
        fun newInstance() = BookmarkFragment()
    }
}
