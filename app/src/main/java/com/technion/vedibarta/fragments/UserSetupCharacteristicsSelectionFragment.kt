package com.technion.vedibarta.fragments

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import com.technion.vedibarta.data.StudentResources
import com.technion.vedibarta.data.viewModels.UserSetupViewModel
import com.technion.vedibarta.login.UserSetupActivity

/**
 * A simple extension of [BubblesSelectionFragment].
 *
 * To be used in [UserSetupActivity] to allow characteristics selection (each category in its own
 * fragment).
 *
 * New instances of this fragment should be instantiated using [newInstance].
 */
class UserSetupCharacteristicsSelectionFragment : BubblesSelectionFragment() {

    companion object {
        private const val CARD_INDEX_KEY = "CARD_INDEX"

        /**
         * Creates new instances of [UserSetupCharacteristicsSelectionFragment].
         *
         * @param cardIdx the index of the wanted characteristics category
         */
        fun newInstance(cardIdx: Int): UserSetupCharacteristicsSelectionFragment {
            val args = Bundle().apply {
                putInt(CARD_INDEX_KEY, cardIdx)
            }

            return UserSetupCharacteristicsSelectionFragment().apply {
                arguments = args
            }
        }
    }

    val viewModel: UserSetupViewModel by activityViewModels()

    override val translator = StudentResources.characteristicsTranslator

    override val categoryCard by lazy {
        StudentResources.characteristicsCardList[requireArguments().getInt(CARD_INDEX_KEY)]
    }

    override val chosenInitially by lazy {
        viewModel.selectedCharacteristics[requireArguments().getInt(CARD_INDEX_KEY)].value!!
    }

    override fun onSelectedBubblesChanged(selected: Set<String>) {
        viewModel.selectedCharacteristics[requireArguments().getInt(CARD_INDEX_KEY)].value = selected
    }
}
