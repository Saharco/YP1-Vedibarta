package com.technion.vedibarta.fragments

import androidx.lifecycle.MutableLiveData
import com.technion.vedibarta.R
import com.technion.vedibarta.data.TeacherResources
import com.technion.vedibarta.data.viewModels.TeacherProfileEditViewModel
import com.technion.vedibarta.utilities.extensions.parentViewModels
import com.technion.vedibarta.utilities.resourcesManagement.Translation
import com.technion.vedibarta.utilities.resourcesManagement.merge
import com.technion.vedibarta.utilities.resourcesManagement.multilingualTextResourceOf

class TeacherProdileEditSubjectsSelectionFragment : BubblesSelectionFragment() {

    private val viewModel: TeacherProfileEditViewModel by parentViewModels()

    override val translator by lazy {
        MutableLiveData(merge(
            TeacherResources.subjects.translator,
            multilingualTextResourceOf(Translation(getString(R.string.teaching_subjects_title), "title"))
        ))
    }

    override val categoryCard = TeacherResources.subjects.card

    override val chosenInitially by lazy { viewModel.subjects }

    override fun onSelectedBubblesChanged(selected: Set<String>) {
        viewModel.subjects = selected
    }
}