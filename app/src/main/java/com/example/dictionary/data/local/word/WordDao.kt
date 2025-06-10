package com.example.dictionary.data.local.word

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.dictionary.model.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("select * from word")
    fun getAll(): Flow<List<Word>>

    @Query("select * from word where title = :title")
    fun findWordByTitle(title: String): Flow<List<Word>>

    @Insert
    fun insertWord(word: Word)

    @Query("delete from word where meaning =:meaning")
    fun deleteWord(meaning: String)
}
