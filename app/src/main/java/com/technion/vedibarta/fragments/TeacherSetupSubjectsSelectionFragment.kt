package com.technion.vedibarta.fragments

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.technion.vedibarta.data.TeacherResources
import com.technion.vedibarta.data.viewModels.TeacherSetupViewModel

class TeacherSetupSubjectsSelectionFragment: BubblesSelectionFragment() {

    private val viewModel: TeacherSetupViewModel by activityViewModels()

    override val translator = MutableLiveData(TeacherResources.subjects.translator)

    override val categoryCard = TeacherResources.subjects.card

    override val chosenInitially by lazy { viewModel.selectedSubjects }

    override fun onSelectedBubblesChanged(selected: Set<String>) {
        viewModel.selectedSubjects = selected
    }
}