package com.technion.vedibarta.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.technion.vedibarta.POJOs.Grade
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.SchoolInfo

class SchoolsAdapter(
    private val addButtonLambda: () -> Unit,
    private val longPressLambda: (v:View) -> Boolean,
    private val schoolsInfoList: MutableList<SchoolInfo>
) :
    RecyclerView.Adapter<SchoolsAdapter.SchoolsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolsViewHolder {
        return if (viewType == 0) {
            val schoolAddButtonView = LayoutInflater.from(parent.context)
                .inflate(R.layout.add_school_card, parent, false)
            SchoolsViewHolder.AddSchoolsViewHolder(schoolAddButtonView, addButtonLambda)

        } else {
            val schoolCardView = LayoutInflater.from(parent.context)
                .inflate(R.layout.teacher_setup_school_card, parent, false)
            SchoolsViewHolder.SchoolsCardViewHolder(schoolCardView, longPressLambda)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position > 0) position else 0
    }

    override fun onBindViewHolder(holder: SchoolsViewHolder, position: Int) {
        when (holder) {
            is SchoolsViewHolder.AddSchoolsViewHolder -> holder.bind()
            is SchoolsViewHolder.SchoolsCardViewHolder -> holder.bind(schoolsInfoList[position - 1])
        }
    }

    override fun getItemCount() = schoolsInfoList.size + 1

    sealed class SchoolsViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        class AddSchoolsViewHolder(itemView: View, private val addButtonLambda: () -> Unit) :
            SchoolsViewHolder(itemView) {

            fun bind() {
                val addSchoolButton = itemView.findViewById<TextView>(R.id.addSchoolButton)
                addSchoolButton.setOnClickListener {
                    addButtonLambda()
                }
            }
        }

        class SchoolsCardViewHolder(
            itemView: View,
            private val longPressLambda: (v: View) -> Boolean
        ) : SchoolsViewHolder(itemView) {
            fun bind(schoolInfo: SchoolInfo) {
                val schoolName = schoolInfo.schoolName.substringBeforeLast(" -")
                itemView.findViewById<TextView>(R.id.schoolNameAndRegion).text =
                    "$schoolName - ${schoolInfo.schoolRegion}"
                schoolInfo.grades.forEach {
                    when (it) {
                        Grade.TENTH -> itemView.findViewById<MaterialCheckBox>(R.id.gradeTenth).isChecked =
                            true
                        Grade.ELEVENTH -> itemView.findViewById<MaterialCheckBox>(R.id.gradeEleventh).isChecked =
                            true
                        Grade.TWELFTH -> itemView.findViewById<MaterialCheckBox>(R.id.gradeTwelfth).isChecked =
                            true
                        else -> {
                        }
                    }
                }
                itemView.findViewById<MaterialCardView>(R.id.teacherCardViewSchool)
                    .setOnLongClickListener {
                        longPressLambda(it)
                    }
            }
        }
    }

}