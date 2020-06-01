package com.technion.vedibarta.data.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

sealed class CategoryCardViewModel {

    abstract val title: LiveData<String>
    abstract val bubbleViewModels: List<BubbleViewModel>
    abstract val showBackground: Boolean

    class Toggleable(
        override val title: LiveData<String>,
        override val bubbleViewModels: List<BubbleViewModel>,
        override val showBackground: Boolean,
        isOpenInitially: Boolean = true
    ): CategoryCardViewModel() {
        private val _isOpen = MutableLiveData(isOpenInitially)

        private val onArrowClickListeners: MutableList<() -> Unit> = mutableListOf()

        val isOpen: LiveData<Boolean> = _isOpen

        fun arrowClicked() {
            _isOpen.value = !_isOpen.value!!
            onArrowClickListeners.forEach { it() }
        }

        fun addOnArrowClickListener(listener: () -> Unit) {
            onArrowClickListeners += listener
        }
    }

    data class NonToggleable(
        override val title: LiveData<String>,
        override val bubbleViewModels: List<BubbleViewModel>,
        override val showBackground: Boolean
    ): CategoryCardViewModel()
}
