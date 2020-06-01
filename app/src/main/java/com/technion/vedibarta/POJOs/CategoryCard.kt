package com.technion.vedibarta.POJOs

import java.io.Serializable

data class CategoryCard(
    val title: String,
    val bubbles: List<Bubble>,
    val showBackgrounds: Boolean,
    val isToggleable: Boolean
) : Serializable