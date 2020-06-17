package com.technion.vedibarta.data.viewModels

import androidx.lifecycle.*
import com.technion.vedibarta.POJOs.Bubble
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import com.technion.vedibarta.utilities.resourcesManagement.toCurrentLanguage
import java.lang.IllegalArgumentException

class BubblesSelectionViewModelFactory(
    private val translator: LiveData<MultilingualTextResource>,
    private val title: String,
    private val bubbles: List<Bubble>,
    private val selectedInitially: Set<String> = emptySet()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(BubblesSelectionViewModel::class.java))
            BubblesSelectionViewModel(
                translator, title, bubbles, selectedInitially
            ) as T else throw IllegalArgumentException()
    }
}

class BubblesSelectionViewModel(
    translator: LiveData<MultilingualTextResource>,
    title: String,
    bubbles: List<Bubble>,
    selectedInitially: Set<String> = emptySet()
) : ViewModel() {

    private val _selectedBubbles = MutableLiveData(selectedInitially)

    val title = Transformations.map(translator) { it.toCurrentLanguage(title) }
    val selectedBubbles: LiveData<Set<String>> = _selectedBubbles

    val bubbleViewModels: List<BubbleViewModel> = bubbles.map { bubble ->
        BubbleViewModel(
            Transformations.map(translator) {it.toCurrentLanguage(bubble.content) },
            Transformations.map(selectedBubbles) { bubble.content in it },
            bubble.background
        ) {
            _selectedBubbles.value = when (bubble.content) {
                in selectedBubbles.value!! -> selectedBubbles.value!! - bubble.content
                else -> selectedBubbles.value!! + bubble.content
            }
        }
    }
}