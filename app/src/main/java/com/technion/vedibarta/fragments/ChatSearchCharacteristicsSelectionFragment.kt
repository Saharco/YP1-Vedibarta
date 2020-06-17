package com.technion.vedibarta.fragments

import androidx.fragment.app.activityViewModels
import com.technion.vedibarta.data.StudentResources
import com.technion.vedibarta.data.viewModels.ChatSearchViewModel

/**
 * A simple extension of [CategorizedBubblesSelectionFragment].
 *
 * To be used in [ChatSearchFragment] to allow categorized characteristics selection.
 */
class ChatSearchCharacteristicsSelectionFragment : CategorizedBubblesSelectionFragment() {

    private val viewModel: ChatSearchViewModel by activityViewModels()

    override val translator = StudentResources.characteristicsTranslator

    override val cards = StudentResources.characteristicsCardList

    override val chosenInitially by lazy { viewModel.selectedCharacteristics }

    override fun onSelectedBubblesChanged(selected: Set<String>) {
        viewModel.selectedCharacteristics = selected
    }
}