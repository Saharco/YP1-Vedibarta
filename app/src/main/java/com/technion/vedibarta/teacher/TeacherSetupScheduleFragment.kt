package com.technion.vedibarta.teacher

import androidx.fragment.app.activityViewModels
import com.technion.vedibarta.POJOs.DayHour
import com.technion.vedibarta.data.viewModels.TeacherSetupViewModel
import com.technion.vedibarta.fragments.ScheduleFragment

class TeacherSetupScheduleFragment : ScheduleFragment() {

    val viewModel: TeacherSetupViewModel by activityViewModels()

    override fun onTimeChanged(time: DayHour, isChecked: Boolean) =
        viewModel.scheduleTimeChanged(time, isChecked)
}
