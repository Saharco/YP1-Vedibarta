package com.technion.vedibarta.fragments

import androidx.fragment.app.activityViewModels
import com.technion.vedibarta.data.StudentResources
import com.technion.vedibarta.data.viewModels.ProfileEditViewModel
import com.technion.vedibarta.userProfile.ProfileEditActivity

/**
 * A simple extension of [CategorizedBubblesSelectionFragment].
 *
 * To be used in [ProfileEditActivity] to allow categorized hobbies selection.
 */
class ProfileEditHobbiesSelectionFragment : CategorizedBubblesSelectionFragment() {

    private val viewModel: ProfileEditViewModel by activityViewModels()

    override val translator = StudentResources.hobbiesTranslator

    override val cards = StudentResources.hobbyCardList

    override val chosenInitially by lazy { viewModel.selectedHobbies }

    override fun onSelectedBubblesChanged(selected: Set<String>) {
        viewModel.selectedHobbies = selected
    }
}