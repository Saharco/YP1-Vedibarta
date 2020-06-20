package com.technion.vedibarta.fragments

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.technion.vedibarta.data.TeacherResources
import com.technion.vedibarta.data.viewModels.TeacherSetupViewModel

class TeacherSetupCharacteristicsSelectionFragment: BubblesSelectionFragment() {

    private val viewModel: TeacherSetupViewModel by activityViewModels()

    override val translator = MutableLiveData(TeacherResources.schoolCharacteristics.translator)

    override val categoryCard = TeacherResources.schoolCharacteristics.card

    override val chosenInitially by lazy { viewModel.selectedCharacteristics }

    override fun onSelectedBubblesChanged(selected: Set<String>) {
        viewModel.selectedCharacteristics = selected
    }
}