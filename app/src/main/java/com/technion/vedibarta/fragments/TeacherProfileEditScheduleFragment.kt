package com.technion.vedibarta.fragments

import com.technion.vedibarta.POJOs.DayHour
import com.technion.vedibarta.data.viewModels.TeacherProfileEditViewModel
import com.technion.vedibarta.utilities.extensions.parentViewModels

class TeacherProfileEditScheduleFragment : ScheduleFragment() {

    val viewModel: TeacherProfileEditViewModel by parentViewModels()

    override val initialSchedule by lazy { viewModel.initialSchedule }

    override fun onTimeChanged(time: DayHour, isChecked: Boolean) =
        viewModel.scheduleTimeChanged(time, isChecked)
}