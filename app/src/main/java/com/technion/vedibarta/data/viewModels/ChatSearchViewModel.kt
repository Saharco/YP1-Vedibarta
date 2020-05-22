package com.technion.vedibarta.data.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.utilities.extensions.handleError
import com.technion.vedibarta.utilities.extensions.handleSuccess
import com.technion.vedibarta.utilities.extensions.handleTimeout
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.TextResource
import java.lang.Class

fun chatSearchViewModelFactory(context: Context) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ChatSearchViewModel::class.java)) ChatSearchViewModel(
            context
        ) as T else throw IllegalArgumentException()
    }
}

class ChatSearchViewModel(private val context: Context) : ViewModel() {

    private val _schoolsName = MutableLiveData<LoadableData<TextResource>>(NormalLoading())
    private val _regionsName = MutableLiveData<LoadableData<TextResource>>(NormalLoading())

    val schoolsName: LiveData<LoadableData<TextResource>> = _schoolsName
    val regionsName: LiveData<LoadableData<TextResource>> = _regionsName

    var grade: Grade = Grade.NONE
    var chosenSchool: TextContainer = Unfilled
    var chosenRegion: TextContainer = Unfilled

    init {
        loadResources()
    }

    private fun loadResources() {
        val resourcesManager = RemoteTextResourcesManager(context)

        resourcesManager.findResource("schools")
            .handleSuccess(_schoolsName)
            .handleError(_schoolsName)
            .handleTimeout(_schoolsName)

        resourcesManager.findResource("regions")
            .handleSuccess(_regionsName)
            .handleError(_regionsName)
            .handleTimeout(_regionsName)
    }
}