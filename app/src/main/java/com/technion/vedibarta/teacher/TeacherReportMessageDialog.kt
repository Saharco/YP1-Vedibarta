package com.technion.vedibarta.teacher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.technion.vedibarta.POJOs.AbuseReport
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.ImageLoader

/**
 * A simple [Fragment] subclass.
 * Use the [TeacherReportMessageDialog.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeacherReportMessageDialog(private val report: AbuseReport) : DialogFragment()
{
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        // Inflate the layout for this fragment
        val imageLoder = ImageLoader()
        val view = inflater.inflate(R.layout.teacher_report_message_dialog, container, false)

        //sender gui
        view.findViewById<TextView>(R.id.reporterName).text = report.senderName
        val senderPic = view.findViewById<ImageView>(R.id.reporterImage)
        imageLoder.loadProfileImage(report.senderPhoto, report.senderGender, senderPic, requireContext())

        //abuser gui
        view.findViewById<TextView>(R.id.reportedName).text = report.abuserName
        val abuserPic = view.findViewById<ImageView>(R.id.reportedImage)
        imageLoder.loadProfileImage(report.abuserPhoto, report.abuserGender, abuserPic, requireContext())

        view.findViewById<TextView>(R.id.reportContent).text = report.content
        return view
    }
}