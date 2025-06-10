package com.example.dictionary.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Word(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "ipa") val ipa: String,
    @ColumnInfo(name = "soundUrl") val soundUrl: String,
    @ColumnInfo(name = "meaning") val meaning: String,
    @ColumnInfo(name = "examples") val examples: List<String>
) : Parcelable
