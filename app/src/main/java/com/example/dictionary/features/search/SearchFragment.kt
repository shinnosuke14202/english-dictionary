package com.example.dictionary.features.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.dictionary.databinding.FragmentSearchBinding
import androidx.appcompat.R.id.search_src_text
import androidx.lifecycle.ViewModelProvider
import com.example.dictionary.R
import com.example.dictionary.features.detail.DetailFragment
import com.example.dictionary.model.Word
import com.example.dictionary.utils.UiState

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: WordViewModel
    private lateinit var imm: InputMethodManager

    private val words = ArrayList<Word>()
    private var index = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[WordViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imm =
            getSystemService(requireContext(), InputMethodManager::class.java) as InputMethodManager

        setupSearchView()
        setupWordMeaning()
    }

    private fun setupWordMeaning() {
        binding.llWord.setOnClickListener {
            if (words.isNotEmpty()) {
                if (index < words.size - 1) {
                    index += 1
                } else {
                    index = 0
                }
                showWordMeaning()
            }
        }
        binding.llSeeMore.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().addToBackStack(null)
                .replace(R.id.clSearch, DetailFragment.newInstance(words[index])).commit()
        }
    }

    private fun setupSearchView() {
        val searchEditText = binding.searchView.findViewById<EditText>(search_src_text)

        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false
            searchEditText.requestFocus()
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchWord(query.toString())
                    imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
                    binding.searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun searchWord(word: String) {
        viewModel.getWordMeaning(word)
        viewModel.wordUiState.observe(viewLifecycleOwner) {
            when (it) {
                UiState.Loading -> {
                    binding.llWord.visibility = View.GONE
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.tvIndex.visibility = View.GONE
                }

                is UiState.Error -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    binding.progressIndicator.visibility = View.GONE
                }

                is UiState.Success<List<Word>> -> {
                    binding.progressIndicator.visibility = View.GONE
                    index = 0
                    words.clear()
                    words.addAll(it.data)
                    showWordMeaning()
                    binding.llWord.visibility = View.VISIBLE
                    binding.tvIndex.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showWordMeaning() {
        if (words.isNotEmpty()) {
            binding.llWord.visibility = View.VISIBLE
            val currentWord = words[index]
            binding.tvWord.text = currentWord.title
            binding.tvMeaning.text = currentWord.meaning
            val currentIndex = "${index + 1}/${words.size}"
            binding.tvIndex.text = currentIndex
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SearchFragment()
    }
}
