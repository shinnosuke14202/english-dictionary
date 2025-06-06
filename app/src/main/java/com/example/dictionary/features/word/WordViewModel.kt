package com.example.dictionary.features.word

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dictionary.data.local.WordRepository
import com.example.dictionary.network.DictionarySite
import com.example.dictionary.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WordViewModel(
    private val repository: WordRepository,
    private val dictionarySite: DictionarySite
) : ViewModel() {

    private val _wordsByTitle = MutableStateFlow<List<Word>>(emptyList())
    val wordsByTitle: StateFlow<List<Word>> = _wordsByTitle

    private val _allWords = MutableStateFlow<List<Word>>(emptyList())
    val allWords: StateFlow<List<Word>> = _allWords

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

    fun getWordsByTitle(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getWordsByTitle(title).collect {
                    withContext(Dispatchers.Main) {
                        _wordsByTitle.value = it
                    }
                }
            } catch (e: Exception) {
                Log.i("EXCEPTION", e.message ?: "Error occurred")
            }
        }
    }

    fun getAllWords() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getAllWords().collect {
                    withContext(Dispatchers.Main) {
                        _allWords.value = it
                    }
                }
            } catch (e: Exception) {
                Log.i("EXCEPTION", e.message ?: "Error occurred")
            }
        }
    }

    fun insertWord(word: Word) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertWord(word)
            } catch (e: Exception) {
                Log.i("EXCEPTION", e.message ?: "Error occurred")
            }
        }
    }

    fun deleteWord(word: Word) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteWord(word)
            } catch (e: Exception) {
                Log.i("EXCEPTION", e.message ?: "Error occurred")
            }
        }
    }
}
