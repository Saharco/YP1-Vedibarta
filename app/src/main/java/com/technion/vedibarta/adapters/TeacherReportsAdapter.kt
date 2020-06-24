package com.technion.vedibarta.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.POJOs.AbuseReport
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.POJOs.Teacher
import com.technion.vedibarta.R
import com.technion.vedibarta.teacher.TeacherReportMessageDialog
import com.technion.vedibarta.utilities.ImageLoader
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.database

class TeacherReportsAdapter(private val context: Context, reports: List<AbuseReport>,
                            private val fragmentManager: FragmentManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private val reports = reports.sortedByDescending { it.time }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val teacherView =
            LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
        return ViewHolder(teacherView, context)
    }

    override fun getItemCount(): Int = reports.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        when (holder)
        {
            is ViewHolder ->
            {
                val report = reports[holder.adapterPosition]
                holder.bind(report)
                holder.view.setOnClickListener {
                    TeacherReportMessageDialog(report).show(fragmentManager,
                                                      "TeacherReportMessageDialog")
                }
            }
        }
    }

    class ViewHolder(val view: View, val context: Context) : RecyclerView.ViewHolder(view)
    {
        fun bind(report: AbuseReport)
        {
            view.findViewById<TextView>(R.id.user_name).text = report.senderName
            view.findViewById<TextView>(R.id.last_message).text = report.content
            view.findViewById<TextView>(R.id.relative_timestamp).text = database.clock.calcRelativeTime(report.time, context)
            val profilePicture = view.findViewById<ImageView>(R.id.user_picture)
            ImageLoader().loadProfileImage(report.senderPhoto, report.senderGender, profilePicture, context)
        }
    }
}