package com.technion.vedibarta.fragments

import androidx.lifecycle.MutableLiveData
import com.technion.vedibarta.R
import com.technion.vedibarta.data.TeacherResources
import com.technion.vedibarta.data.viewModels.TeacherSearchMatchViewModel
import com.technion.vedibarta.utilities.extensions.parentViewModels
import com.technion.vedibarta.utilities.resourcesManagement.Translation
import com.technion.vedibarta.utilities.resourcesManagement.merge
import com.technion.vedibarta.utilities.resourcesManagement.multilingualTextResourceOf

class TeacherSearchMatchSchoolCharacteristicsSelectionFragment : BubblesSelectionFragment() {

    private val viewModel: TeacherSearchMatchViewModel by parentViewModels()

    override val translator by lazy {
        MutableLiveData(merge(
            TeacherResources.schoolCharacteristics.translator,
            multilingualTextResourceOf(Translation(getString(R.string.schools_characteristics_title), "title"))
        ))
    }

    override val categoryCard = TeacherResources.schoolCharacteristics.card

    override val chosenInitially by lazy { viewModel.characteristics }

    override fun onSelectedBubblesChanged(selected: Set<String>) {
        viewModel.characteristics = selected
    }
}