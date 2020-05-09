package com.technion.vedibarta.POJOs

sealed class LoadableData<out T>

sealed class Loading<T> : LoadableData<T>()

class SlowLoadingEvent<T> : Loading<T>() {
    private var _handled = false

    val handled
        get() = _handled.apply { _handled = true }
}

class NormalLoading<T> : Loading<T>()

data class Loaded<T>(val data: T) : LoadableData<T>()

data class Error<T>(val reason: String?) : LoadableData<T>()