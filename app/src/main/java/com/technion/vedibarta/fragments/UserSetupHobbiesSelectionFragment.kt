package com.technion.vedibarta.fragments

import androidx.fragment.app.activityViewModels
import com.technion.vedibarta.data.StudentResources
import com.technion.vedibarta.data.viewModels.UserSetupViewModel
import com.technion.vedibarta.login.UserSetupActivity

/**
 * A simple extension of [CategorizedBubblesSelectionFragment].
 *
 * To be used in [UserSetupActivity] to allow categorized hobbies selection.
 */
class UserSetupHobbiesSelectionFragment : CategorizedBubblesSelectionFragment() {

    private val viewModel: UserSetupViewModel by activityViewModels()

    override val translator = StudentResources.hobbiesTranslator

    override val cards = StudentResources.hobbyCardList

    override val chosenInitially by lazy { viewModel.selectedHobbies }

    override fun onSelectedBubblesChanged(selected: Set<String>) {
        viewModel.selectedHobbies = selected
    }
}