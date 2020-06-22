package com.technion.vedibarta.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.technion.vedibarta.R
import com.technion.vedibarta.teacher.TeacherReportMessageDialog
import kotlinx.android.synthetic.main.fragment_teacher_reports_list.*

/**
 * A simple [Fragment] subclass.
 */
class TeacherReportsListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_teacher_reports_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        testButton.setOnClickListener {
            TeacherReportMessageDialog().show(parentFragmentManager, "TeacherReportMessageDialog")
        }
    }
}
