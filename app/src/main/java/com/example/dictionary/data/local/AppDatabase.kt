package com.example.dictionary.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dictionary.features.word.Word
import com.example.dictionary.utils.Converters

@Database(entities = [Word::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        private const val DATABASE_NAME = "words.db"

        // Always give the latest value, even if it was set by another thread
        @Volatile
        private var _instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            // Ensures that only one thread at a time can run the code inside the block.
            synchronized(this) {
                var instance = _instance
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, AppDatabase::class.java, DATABASE_NAME
                    ).build()
                    _instance = instance
                }
                return instance
            }
        }
    }
}
