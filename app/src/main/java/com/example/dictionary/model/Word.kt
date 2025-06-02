package com.example.dictionary.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Word(
    val title: String,
    val type: String,
    val ipa: String,
    val soundUrl: String,
    val meaning: String,
    val examples: List<String>
) : Parcelable
