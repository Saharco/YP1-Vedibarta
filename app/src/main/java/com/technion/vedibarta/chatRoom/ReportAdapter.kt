package com.technion.vedibarta.chatRoom

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.Teacher
import com.technion.vedibarta.R
import com.technion.vedibarta.database.StudentData
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.utilities.ImageLoader
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.student

class ReportAdapter(private val context: Context, teachers: List<Teacher>): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private val teachers = teachers.sortedBy { it.name }
    var selectedTeacher: Teacher? = null
    private var selectedId: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val teacherView =
            LayoutInflater.from(parent.context).inflate(R.layout.report_teachers_list_card, parent, false)
        return ViewHolder(teacherView, context)
    }

    override fun getItemCount(): Int = teachers.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        when(holder)
        {
            is ViewHolder -> {
                val teacher = teachers[holder.adapterPosition]
                holder.bind(teacher)
                holder.view.setOnClickListener {
                    Log.d("Reports", "selected id: $selectedId")
                    if (selectedId == holder.adapterPosition) {
                        // Deselect currently selected teacher
                        selectedTeacher = null
                        selectedId = -1
                        notifyDataSetChanged()
                    }
                    else {
                        selectedTeacher = teacher
                        selectedId = holder.adapterPosition
                        notifyDataSetChanged()
                    }
                }
                if (selectedId == holder.adapterPosition) {
                    holder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight))
                } else {
                    holder.view.setBackgroundColor(Color.WHITE)
                }
            }
        }
    }



    class ViewHolder(val view: View, val context: Context): RecyclerView.ViewHolder(view)
    {
        fun bind(teacher: Teacher)
        {
            view.findViewById<TextView>(R.id.teacher_name).text = teacher.name
            val profilePicture = view.findViewById<ImageView>(R.id.teacher_picture)
            ImageLoader().loadProfileImage(teacher.photo, teacher.gender, profilePicture, context)
        }
    }
}