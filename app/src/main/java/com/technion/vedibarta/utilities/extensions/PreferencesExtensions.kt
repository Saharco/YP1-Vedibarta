package com.technion.vedibarta.utilities.extensions

import android.content.SharedPreferences.Editor
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Language

const val GENDER_KEY = "Gender"
const val Language_KEY = "Lang"

fun Editor.putGender(gender: Gender): Editor =
    this.putString(GENDER_KEY, gender.toString())

fun Editor.putLanguage(language: String): Editor =
    this.putString(Language_KEY, language)
