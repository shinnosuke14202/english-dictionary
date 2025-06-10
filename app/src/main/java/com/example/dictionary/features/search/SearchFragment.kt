package com.example.dictionary.features.search

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.R.id.search_src_text
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.dictionary.R
import com.example.dictionary.data.local.AppDatabase
import com.example.dictionary.data.local.word.WordRepository
import com.example.dictionary.databinding.FragmentSearchBinding
import com.example.dictionary.features.detail.DetailFragment
import com.example.dictionary.features.word.WordViewModel
import com.example.dictionary.features.word.WordViewModelFactory
import com.example.dictionary.model.Word
import com.example.dictionary.network.DictionarySite
import com.example.dictionary.utils.HISTORY_LIST
import com.example.dictionary.utils.HISTORY_PREFS
import com.example.dictionary.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: WordViewModel
    private lateinit var imm: InputMethodManager

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var popup: ListPopupWindow
    private lateinit var suggestAdapter: ArrayAdapter<String>

    private val words = ArrayList<Word>()
    private val suggestions = mutableListOf<String>()

    private val db by lazy {
        AppDatabase.getInstance(requireContext())
    }
    private val dictionarySite = DictionarySite()
    private val wordRepository by lazy {
        WordRepository(db.wordDao())
    }

    private var index = 0
    private var searchJob: Job? = null
    private var showPopUp = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences =
            requireContext().getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE)

        viewModel = ViewModelProvider(
            this,
            WordViewModelFactory(dictionarySite, wordRepository)
        )[WordViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imm =
            getSystemService(requireContext(), InputMethodManager::class.java) as InputMethodManager

        setupSearchView()
        setupPopUp()
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

    private fun setupPopUp() {
        popup = ListPopupWindow(requireContext())

        // Set margin top (vertical offset)
        val marginTopPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, resources.displayMetrics
        ).toInt()
        popup.verticalOffset = marginTopPx

        // Set popup height dynamically
        val maxHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 300f, resources.displayMetrics
        ).toInt()

        popup.height = maxHeightPx

        popup.anchorView = binding.searchView
        popup.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.rounded_border
            )
        )
        suggestAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, suggestions
        )
        popup.setAdapter(
            suggestAdapter
        )
        popup.setOnItemClickListener { _, _, position, _ ->
            binding.searchView.setQuery(suggestions[position], true)
            imm.hideSoftInputFromWindow(
                binding.searchView.windowToken, 0
            )
            popup.dismiss()
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
                    popup.dismiss()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty().trim()

                searchJob?.cancel()
                if (query.isNotEmpty()) {
                    searchJob = lifecycleScope.launch {
                        delay(300)
                        getWordSuggestions(query)
                        if (!popup.isShowing && showPopUp) {
                            popup.show()
                        }
                    }
                } else {
                    if (popup.isShowing) popup.dismiss()
                }

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
                    showPopUp = false
                }

                is UiState.Error -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    binding.progressIndicator.visibility = View.GONE
                    showPopUp = true
                }

                is UiState.Success<List<Word>> -> {
                    binding.progressIndicator.visibility = View.GONE
                    index = 0
                    words.clear()
                    words.addAll(it.data)
                    showWordMeaning()
                    binding.llWord.visibility = View.VISIBLE
                    binding.tvIndex.visibility = View.VISIBLE
                    showPopUp = true
                }
            }
        }
        addToHistory(word)
    }

    private fun showWordMeaning() {
        if (words.isNotEmpty()) {
            searchJob?.cancel()
            binding.llWord.visibility = View.VISIBLE
            val currentWord = words[index]
            binding.tvWord.text = currentWord.title
            binding.tvMeaning.text = currentWord.meaning
            val currentIndex = "${index + 1}/${words.size}"
            binding.tvIndex.text = currentIndex
        }
    }

    private fun getWordSuggestions(word: String) {
        viewModel.getWordSuggestion(word)
        viewModel.suggestUiState.observe(viewLifecycleOwner) {
            when (it) {
                UiState.Loading -> {}

                is UiState.Error -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                }

                is UiState.Success<List<String>> -> {
                    binding.progressIndicator.visibility = View.GONE
                    index = 0
                    suggestions.clear()
                    suggestions.addAll(it.data)
                    suggestAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun addToHistory(title: String) {
        val history = sharedPreferences.getString(HISTORY_LIST, "") ?: ""
        lifecycleScope.launch(Dispatchers.IO) {
            sharedPreferences.edit { putString(HISTORY_LIST, "${title},${history}") }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SearchFragment()
    }
}
