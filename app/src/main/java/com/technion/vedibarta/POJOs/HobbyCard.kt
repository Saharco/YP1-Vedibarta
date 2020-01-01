package com.technion.vedibarta.POJOs

import java.io.Serializable

data class HobbyCard(val title: String, val hobbies: List<Pair<Int, String>>) : Serializable