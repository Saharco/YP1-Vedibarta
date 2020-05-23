package com.technion.vedibarta.data.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import java.lang.IllegalArgumentException

fun chooseElementsViewModelFactory(resource: LiveData<MultilingualTextResource>?, chosenList: MutableList<String>) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(ChooseElementsViewModel::class.java)) ChooseElementsViewModel(
                resource, chosenList
            ) as T else throw IllegalArgumentException()
        }
    }


class ChooseElementsViewModel(
    val resource: LiveData<MultilingualTextResource>?,
    val chosenList: MutableList<String>
) : ViewModel(){

}