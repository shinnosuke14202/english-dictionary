package com.example.dictionary.features.search

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dictionary.model.Word
import com.example.dictionary.network.DictionarySite
import com.example.dictionary.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WordViewModel : ViewModel() {

    private val dictionarySite = DictionarySite()

    private val _wordUiState = MutableLiveData<UiState<List<Word>>>(UiState.Loading)
    val wordUiState get() = _wordUiState

    private val _suggestUiState = MutableLiveData<UiState<List<String>>>(UiState.Loading)
    val suggestUiState get() = _suggestUiState

    fun getWordMeaning(word: String) {
        _wordUiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    dictionarySite.getWordMeaning(word)
                }
                _wordUiState.value = UiState.Success(results)
            } catch (e: Exception) {
                _wordUiState.value = UiState.Error(e.message ?: "An error occurred!")

            }
        }
    }

    fun getWordSuggestion(word: String) {
        viewModelScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    dictionarySite.getSearchSuggestions(word)
                }
                _suggestUiState.value = UiState.Success(results)
            } catch (e: Exception) {
                _suggestUiState.value = UiState.Error(e.message ?: "An error occurred!")
            }
        }
    }
}
