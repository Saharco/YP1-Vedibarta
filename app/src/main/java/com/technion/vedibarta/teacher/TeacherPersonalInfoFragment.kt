package com.technion.vedibarta.teacher

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isInvisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.fragment_teacher_personal_info.*
import kotlinx.android.synthetic.main.teacher_setup_school_card.*

class TeacherPersonalInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacher_personal_info, container, false)

        view.findViewById<NestedScrollView>(R.id.teacherPersonalInfoScrollView).isNestedScrollingEnabled = false
        val schoolList = view.findViewById<RecyclerView>(R.id.schoolList)
        schoolList.isNestedScrollingEnabled = false
        schoolList.layoutManager = LinearLayoutManager(activity)
        val adapter = SchoolsAdapter(schoolList)
        schoolList.adapter = adapter

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TeacherPersonalInfoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TeacherPersonalInfoFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}

class SchoolsAdapter(private val recyclerView: RecyclerView): RecyclerView.Adapter<SchoolsAdapter.SchoolsViewHolder>() {
    private val TAG = "SchoolsAdapter"
    private var numOfSchools = 1

    private val schoolCardsList = mutableListOf<Int>()

    class SchoolsViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val addSchool = v.findViewById<FloatingActionButton>(R.id.addSchoolButton)
        val addSchoolText = v.findViewById<TextView>(R.id.addSchoolText)
        val removeSchool = v.findViewById<ImageButton>(R.id.removeSchoolButton)
        var dummy: Int = 0

        fun makeAddSchoolInvisible() {
            addSchool.isInvisible = true
            addSchoolText.isInvisible = true
        }

        fun makeAddSchoolVisible() {
            addSchool.isInvisible = false
            addSchoolText.isInvisible = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolsViewHolder {
        schoolCardsList.add(1)
        val v = LayoutInflater.from(parent.context).inflate(R.layout.teacher_setup_school_card, parent, false)
        val holder = SchoolsViewHolder(v)
        if (numOfSchools == 1) {
            holder.removeSchool.isInvisible = true
        }
        return holder
    }

    override fun onBindViewHolder(holder: SchoolsViewHolder, position: Int) {
        holder.addSchool.setOnClickListener {
            numOfSchools++
            Log.d(TAG, "numOfSchools: $numOfSchools")
            Log.d(TAG, "position: $position")
            schoolCardsList.add(1)
            holder.dummy = schoolCardsList[position]
            holder.makeAddSchoolInvisible()
            holder.removeSchool.isInvisible = false
            notifyItemInserted(position+1)
        }
        holder.removeSchool.setOnClickListener {
            numOfSchools--
            Log.d(TAG, "numOfSchools: $numOfSchools")
            Log.d(TAG, "position: $position")

            if (position == numOfSchools) {
                val prevHolder = recyclerView.findViewHolderForAdapterPosition(position - 1)
                (prevHolder as SchoolsViewHolder).makeAddSchoolVisible()
            }
            if (numOfSchools == 1) {
                val prevHolder = recyclerView.findViewHolderForAdapterPosition(0)
                (prevHolder as SchoolsViewHolder).removeSchool.isInvisible = true
            }

            schoolCardsList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount() = numOfSchools
}
