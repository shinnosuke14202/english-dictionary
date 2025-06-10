package com.example.dictionary.data.local.word

import com.example.dictionary.model.Word

class WordRepository(private val wordDao: WordDao) {
    fun getAllWords() = wordDao.getAll()
    fun getWordsByTitle(title: String) = wordDao.findWordByTitle(title)
    fun insertWord(word: Word) = wordDao.insertWord(word)
    fun deleteWord(word: Word) = wordDao.deleteWord(word.meaning)
}
