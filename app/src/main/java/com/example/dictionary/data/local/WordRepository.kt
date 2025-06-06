package com.example.dictionary.data.local

import com.example.dictionary.features.word.Word

class WordRepository(private val wordDao: WordDao) {
    fun getAllWords() = wordDao.getAll()
    fun getWordsByTitle(title: String) = wordDao.findWordByTitle(title)
    fun insertWord(word: Word) = wordDao.insertWord(word)
    fun deleteWord(word: Word) = wordDao.deleteWord(word.meaning)
}
