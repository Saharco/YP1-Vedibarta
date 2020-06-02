package com.technion.vedibarta.data.viewModels

import androidx.lifecycle.ViewModel
import com.google.android.material.card.MaterialCardView
import com.technion.vedibarta.POJOs.Class
import com.technion.vedibarta.POJOs.TextContainer
import com.technion.vedibarta.POJOs.Unfilled

class TeacherClassListViewModel :ViewModel(){
    //TODO Change from null to default photo
    var chosenClassPicture : String? = null

    var chosenClassDescription: TextContainer = Unfilled
    var chosenClassName: TextContainer = Unfilled

    var itemActionBarEnabled = false
    var selectedItems = 0
    val selectedItemsList = mutableListOf<MaterialCardView>()
    //TODO: Initialize class list from database instead of an empty list
    val classesList = mutableListOf<Class>()

}