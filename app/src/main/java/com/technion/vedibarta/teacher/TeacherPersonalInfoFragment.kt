package com.technion.vedibarta.teacher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.technion.vedibarta.POJOs.Grade
import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.fragment_teacher_personal_info.*

class TeacherPersonalInfoFragment : Fragment() {

    //TODO: move into ViewModel
    val schoolsInfoList: MutableList<SchoolInfo> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacher_personal_info, container, false)

        view.findViewById<NestedScrollView>(R.id.teacherPersonalInfoScrollView).isNestedScrollingEnabled =
            false
        val schoolList = view.findViewById<RecyclerView>(R.id.schoolList)
        schoolList.isNestedScrollingEnabled = false
        schoolList.layoutManager = LinearLayoutManager(activity)
        val adapter = SchoolsAdapter({ onAddSchoolButtonClick() }, schoolsInfoList)
        schoolList.adapter = adapter

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TeacherPersonalInfoFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    fun onAddSchoolButtonClick() {
        val dialog = MaterialDialog(requireContext())
        dialog.cornerRadius(20f)
            .noAutoDismiss()
            .customView(R.layout.fragment_add_school_dialog)
            .show {
                initMaterialDialog(this)

            }
    }

    private fun initMaterialDialog(materialDialog: MaterialDialog) {
        val grades = mutableListOf<Grade>()
        materialDialog.findViewById<MaterialCheckBox>(R.id.gradeTenth)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    grades.add(Grade.TENTH)
                else
                    grades.remove(Grade.TENTH)
            }
        materialDialog.findViewById<MaterialCheckBox>(R.id.gradeEleventh)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    grades.add(Grade.ELEVENTH)
                else
                    grades.remove(Grade.ELEVENTH)
            }
        materialDialog.findViewById<MaterialCheckBox>(R.id.gradeTwelfth)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    grades.add(Grade.TWELFTH)
                else
                    grades.remove(Grade.TWELFTH)
            }
        val name = materialDialog.findViewById<AutoCompleteTextView>(R.id.schoolListSpinner)
        val region = materialDialog.findViewById<AutoCompleteTextView>(R.id.regionListSpinner)
        materialDialog.findViewById<MaterialButton>(R.id.addButton)
            .setOnClickListener {
                schoolsInfoList.add(
                    SchoolInfo(
                        name.text.toString(),
                        region.text.toString(),
                        grades
                    )
                )
                schoolList.adapter!!.notifyItemInserted(schoolsInfoList.size)
                materialDialog.dismiss()
            }

        materialDialog.findViewById<MaterialButton>(R.id.cancelButton)
            .setOnClickListener { materialDialog.dismiss() }
    }
}

class SchoolsAdapter(
    private val addButtonLambda: () -> Unit,
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
            SchoolsViewHolder.SchoolsCardViewHolder(schoolCardView)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position > 0) position else 0
    }

    override fun onBindViewHolder(holder: SchoolsViewHolder, position: Int) {
        when (holder) {
            is SchoolsViewHolder.AddSchoolsViewHolder -> holder.bind()
            is SchoolsViewHolder.SchoolsCardViewHolder -> holder.bind(schoolsInfoList[position-1])
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

        class SchoolsCardViewHolder(itemView: View) : SchoolsViewHolder(itemView) {
            fun bind(schoolInfo: SchoolInfo) {

            }

        }
    }

}

data class SchoolInfo(
    val schoolName: String,
    val schoolRegion: String,
    val grades: List<Grade>
)
