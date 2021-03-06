package com.technion.vedibarta.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.technion.vedibarta.POJOs.Grade
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.SchoolInfo

class SchoolsAdapter(
    private val schoolsInfoList: List<SchoolInfo>,
    private val onEditClickedListener: (Int) -> Unit
) : RecyclerView.Adapter<SchoolsAdapter.SchoolsCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolsCardViewHolder {
        val schoolCardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.teacher_setup_school_card, parent, false)
        return SchoolsCardViewHolder(schoolCardView)
    }

    override fun onBindViewHolder(holder: SchoolsCardViewHolder, position: Int) {
        holder.bind(schoolsInfoList[position]) { onEditClickedListener(position) }
    }

    override fun getItemCount() = schoolsInfoList.size

    class SchoolsCardViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(schoolInfo: SchoolInfo, onEditClickedListener: () -> Unit) {
            val schoolName = schoolInfo.schoolName.substringBeforeLast(" -")
            itemView.findViewById<TextView>(R.id.schoolNameAndRegion).text =
                "$schoolName - ${schoolInfo.schoolRegion}"
            schoolInfo.grades.forEach {
                when (it) {
                    Grade.TENTH -> itemView.findViewById<MaterialCheckBox>(R.id.gradeTenth).isChecked = true
                    Grade.ELEVENTH -> itemView.findViewById<MaterialCheckBox>(R.id.gradeEleventh).isChecked = true
                    Grade.TWELFTH -> itemView.findViewById<MaterialCheckBox>(R.id.gradeTwelfth).isChecked = true
                    else -> {
                    }
                }
            }

            val editButton = itemView.findViewById<AppCompatImageButton>(R.id.edit_school)
            editButton.setOnClickListener { onEditClickedListener() }
        }
    }
}