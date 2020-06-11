package com.technion.vedibarta.fragments

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Transformations
import com.technion.vedibarta.POJOs.CategoryCard
import com.technion.vedibarta.data.StudentResources
import com.technion.vedibarta.data.viewModels.UserSetupViewModel

class UserSetupHobbiesSelectionFragment : CategorizedBubblesSelectionFragment() {

    private val viewModel: UserSetupViewModel by activityViewModels()

    override val translator = StudentResources.hobbiesTranslator

    override val cards = StudentResources.hobbyCardList

    override val chosenInitially by lazy { viewModel.selectedHobbies }

    override fun onSelectedBubblesChanged(selected: Set<String>) {
        viewModel.selectedHobbies = selected
    }
}