package com.technion.vedibarta.fragments

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.technion.vedibarta.R
import com.technion.vedibarta.data.TeacherResources
import com.technion.vedibarta.data.viewModels.TeacherSetupViewModel
import com.technion.vedibarta.utilities.resourcesManagement.Translation
import com.technion.vedibarta.utilities.resourcesManagement.merge
import com.technion.vedibarta.utilities.resourcesManagement.multilingualTextResourceOf

class TeacherSetupCharacteristicsSelectionFragment : BubblesSelectionFragment() {

    private val viewModel: TeacherSetupViewModel by activityViewModels()

    override val translator by lazy {
        MutableLiveData(merge(
            TeacherResources.schoolCharacteristics.translator,
            multilingualTextResourceOf(Translation(getString(R.string.schools_characteristics_title), "title"))
        ))
    }

    override val categoryCard = TeacherResources.schoolCharacteristics.card

    override val chosenInitially by lazy { viewModel.selectedCharacteristics }

    override fun onSelectedBubblesChanged(selected: Set<String>) {
        viewModel.selectedCharacteristics = selected
    }
}