package com.example.dictionary.features.word

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dictionary.data.local.word.WordRepository
import com.example.dictionary.network.DictionarySite

class WordViewModelFactory(
    private val dictionarySite: DictionarySite,
    private val wordRepository: WordRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordViewModel::class.java)) {
            return WordViewModel(wordRepository, dictionarySite) as T
        }
        throw IllegalArgumentException("Unknown Viewmodel")
    }
}
