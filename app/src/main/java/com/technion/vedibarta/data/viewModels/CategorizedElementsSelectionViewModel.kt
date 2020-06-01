package com.technion.vedibarta.data.viewModels

import androidx.lifecycle.*
import com.technion.vedibarta.POJOs.CategoryCard
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import com.technion.vedibarta.utilities.resourcesManagement.toCurrentLanguage

class CategorizedBubblesSelectionViewModelFactory(
    private val translator: LiveData<MultilingualTextResource>,
    private val cards: List<CategoryCard>,
    private val selectedInitially: Set<String> = emptySet()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CategorizedBubblesSelectionViewModel::class.java))
            CategorizedBubblesSelectionViewModel(
                translator, cards, selectedInitially
            ) as T else throw IllegalArgumentException()
    }
}

class CategorizedBubblesSelectionViewModel(
    translator: LiveData<MultilingualTextResource>,
    cards: List<CategoryCard>,
    selectedInitially: Set<String> = emptySet()
) : ViewModel() {

    private val _selectedBubbles = MutableLiveData(selectedInitially)

    val selectedBubbles: LiveData<Set<String>> = _selectedBubbles

    val categoryCardViewModels = cards.map { card ->
        val titleLiveData = Transformations.map(translator) { it.toCurrentLanguage(card.title) }
        val elementViewModels = card.bubbles.map { bubble ->

            BubbleViewModel(
                Transformations.map(translator) { it.toCurrentLanguage(bubble.content) },
                Transformations.map(selectedBubbles) { bubble.content in it },
                bubble.background
            ) {
                _selectedBubbles.value = when (bubble.content) {
                    in selectedBubbles.value!! -> selectedBubbles.value!! - bubble.content
                    else -> selectedBubbles.value!! + bubble.content
                }
            }
        }

        return@map if (card.isToggleable)
            CategoryCardViewModel.Toggleable(
                titleLiveData,
                elementViewModels,
                card.showBackgrounds
            )
        else
            CategoryCardViewModel.NonToggleable(
                titleLiveData,
                elementViewModels,
                card.showBackgrounds
            )
    }
}
