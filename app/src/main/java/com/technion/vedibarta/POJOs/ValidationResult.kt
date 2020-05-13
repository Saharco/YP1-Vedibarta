package com.technion.vedibarta.POJOs

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val resId: Int) : ValidationResult()
}