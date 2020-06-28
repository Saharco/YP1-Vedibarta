package com.technion.vedibarta.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.technion.vedibarta.POJOs.DayHour
import com.technion.vedibarta.POJOs.emptyTimetable
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.ScheduleAdapter
import kotlinx.android.synthetic.main.fragment_teacher_schedule.*

abstract class ScheduleFragment : Fragment() {

    abstract fun onTimeChanged(time: DayHour, isChecked: Boolean)

    protected open val initialSchedule = emptyTimetable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_teacher_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        schedule.adapter = ScheduleAdapter(requireContext(), ::onTimeChanged, initialSchedule)
        val layoutManager = GridLayoutManager(requireContext(), 1 * 7 + 6 * 4)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) = if (position % 7 == 0) 7 else 4
        }
        schedule.layoutManager = layoutManager
    }
}