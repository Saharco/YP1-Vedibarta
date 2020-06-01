package com.technion.vedibarta.data.viewModels

import androidx.lifecycle.LiveData
import java.io.File

class BubbleViewModel (
    val content: LiveData<String>,
    val marked: LiveData<Boolean>,
    val background: File? = null,
    val onClick: () -> Unit = {}
)
