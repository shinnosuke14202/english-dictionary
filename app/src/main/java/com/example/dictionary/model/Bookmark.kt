package com.example.dictionary.model

data class Bookmark(
    private val id: Long,
    private val word: Word,
    private val isBookmark: Boolean,
)
