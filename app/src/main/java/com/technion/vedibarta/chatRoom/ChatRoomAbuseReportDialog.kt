package com.technion.vedibarta.chatRoom

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.AbuseReport
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.QuestionGeneratorCategoryAdapter
import com.technion.vedibarta.database.AbuseReporter
import com.technion.vedibarta.database.StudentData
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.student
import kotlinx.android.synthetic.main.question_generator_dialog.*
import kotlinx.android.synthetic.main.report_abuse_dialog.*

/***
 * class in charge of the reporting functionality in a specific chat
 */
class ChatRoomAbuseReportDialog(private val partnerId: String,
                                private val partnerName: String,
                                private val partnerPhoto: String?,
                                private val partnerGender: Gender) : DialogFragment()
{
    private lateinit var adapter: ReportAdapter
    private lateinit var listener: AbuseReportDialogListener
    private val reporter = AbuseReporter()

    interface AbuseReportDialogListener
    {
        fun onAbuseTypeClick(dialog: DialogFragment)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val inflatedView = inflater.inflate(R.layout.report_abuse_dialog, container, false)
        dialog?.setCanceledOnTouchOutside(true)
        return inflatedView
    }

    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        try
        {
            listener = context as AbuseReportDialogListener
        }
        catch (e: ClassCastException)
        {
            Log.d("reportAbuseDialog", e.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        configureRecycler()

        reportButton.text = getString(R.string.chat_room_abuse_dialog_next_button)
        reportButton.setOnClickListener {
            if (adapter.selectedTeacher == null)
                Toast.makeText(context,
                               getString(R.string.chat_room_abuse_dialog_choose_teacher_error),
                               Toast.LENGTH_SHORT).show()
            else if (reportViewFlipper.displayedChild == 0)
            {
                Log.d("Reports", "Selected teacher is ${adapter.selectedTeacher!!.name}")
                reportViewFlipper.showNext()
                reportButton.text = getString(R.string.chat_room_abuse_dialog_report_button)
                reportBackButton.visibility = View.VISIBLE
            }
            else
            {
                val report = reportText.text.toString()
                if (report.isNotBlank())
                {
                    reporter.report(student!!.uid,
                                    student!!.name,
                                    student!!.photo,
                                    student!!.gender,
                                    partnerId,
                                    partnerName,
                                    partnerPhoto,
                                    partnerGender,
                                    adapter.selectedTeacher!!.uid,
                                    report)
                        .addOnSuccessListener {
                            Toast.makeText(context,
                                           getString(R.string.chat_room_abuse_dialog_report_sent),
                                           Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Log.d("wtf", "${it.message}, cause: ${it.cause}")
                            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show()
                        }
                        .addOnCompleteListener {
                            dismiss()
                        }
                }
                else
                {
                    Toast.makeText(context,
                                   getString(R.string.chat_room_abuse_dialog_empty_report_error),
                                   Toast.LENGTH_SHORT).show()
                }
            }
        }

        reportBackButton.setOnClickListener {
            reportViewFlipper.showPrevious()
            reportBackButton.visibility = View.GONE
            reportButton.text = getString(R.string.chat_room_abuse_dialog_next_button)
        }

        reportAbuseDismissButton.setOnClickListener {
            dismiss()
        }
    }

    private fun configureRecycler()
    {
        StudentData(student!!).teachers
            .addOnSuccessListener { teachers ->
                adapter = ReportAdapter(this.requireContext(), teachers.distinct())
                teachersForReportList.adapter = adapter
                teachersForReportList.layoutManager = LinearLayoutManager(this.context)
            }
    }
}