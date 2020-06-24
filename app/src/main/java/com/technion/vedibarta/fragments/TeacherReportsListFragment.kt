package com.technion.vedibarta.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.technion.vedibarta.POJOs.AbuseReport
import com.technion.vedibarta.POJOs.Student

import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.TeacherReportsAdapter
import com.technion.vedibarta.data.TeacherMeta.Companion.teacher
import com.technion.vedibarta.database.TeacherData
import com.technion.vedibarta.matching.impl.DocumentsMatcherImpl
import com.technion.vedibarta.teacher.TeacherReportMessageDialog
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.database
import com.technion.vedibarta.utilities.extensions.resultToListOf
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
        configureRecycler()
    }

    private fun configureRecycler()
    {
        TeacherData(teacher).receivedReports
            .continueWith { getReports ->
                val reports = getReports.result ?: emptyList()
                val adapter = TeacherReportsAdapter(this.requireContext(), reports, parentFragmentManager)
                teacherReportsList.adapter = adapter
                teacherReportsList.layoutManager = LinearLayoutManager(this.context)
            }
    }
}
