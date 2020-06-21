package com.technion.vedibarta.teacher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.ScheduleAdapter
import com.technion.vedibarta.data.viewModels.TeacherSetupViewModel
import kotlinx.android.synthetic.main.fragment_teacher_schedule.*

class TeacherScheduleFragment : Fragment() {

    private val viewModel: TeacherSetupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_teacher_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        schedule.adapter = ScheduleAdapter(requireContext()) { _, _, _ -> }
        schedule.numColumns = 8
    }
}
