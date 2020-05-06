package com.technion.vedibarta.POJOs

sealed class TextContainer

object Unfilled : TextContainer()

data class Filled(val text: String) : TextContainer()