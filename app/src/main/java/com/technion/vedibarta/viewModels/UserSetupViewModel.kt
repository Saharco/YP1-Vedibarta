package com.technion.vedibarta.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.technion.vedibarta.POJOs.*
import java.lang.Class
import java.lang.IllegalArgumentException

fun userSetupViewModelFactory(userId: String) = object :ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(UserSetupViewModel::class.java)) UserSetupViewModel(userId) as T else throw IllegalArgumentException()
    }
}

class UserSetupViewModel(userId: String) : ViewModel() {

    val uid = userId
    var gender: Gender = Gender.NONE
    var grade: Grade = Grade.NONE
    var chosenFirstName: TextContainer = Unfilled
    var chosenLastName: TextContainer = Unfilled
    var chosenSchool: TextContainer = Unfilled
    var chosenRegion: TextContainer = Unfilled

    val chosenHobbies = mutableListOf<String>()
    val chosenCharacteristics = mutableMapOf<String, Boolean>()




}

