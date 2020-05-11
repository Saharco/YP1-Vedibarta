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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.fragment_teacher_personal_info.*
import kotlinx.android.synthetic.main.teacher_setup_school_card.*

class TeacherPersonalInfoFragment : Fragment() {
    private var numOfSchools: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacher_personal_info, container, false)

        val schoolList = view.findViewById<RecyclerView>(R.id.schoolList)
        schoolList.isNestedScrollingEnabled = false
        schoolList.layoutManager = LinearLayoutManager(activity)
        val adapter = SchoolsAdapter(numOfSchools)
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

class SchoolsAdapter(private var numOfSchools: Int): RecyclerView.Adapter<SchoolsAdapter.SchoolsViewHolder>() {
    private val TAG = "SchoolsAdapter"

    private val schoolCardsList = mutableListOf<Int>()

    class SchoolsViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val addSchool = v.findViewById<FloatingActionButton>(R.id.addSchoolButton)
        val removeSchool = v.findViewById<ImageButton>(R.id.removeSchoolButton)
        var garbage: Int = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolsViewHolder {
        schoolCardsList.add(1)
        val v = LayoutInflater.from(parent.context).inflate(R.layout.teacher_setup_school_card, parent, false)
        return SchoolsViewHolder(v)
    }

    override fun onBindViewHolder(holder: SchoolsViewHolder, position: Int) {
        holder.addSchool.setOnClickListener {
            Log.d(TAG, "numOfSchools: $numOfSchools")
            numOfSchools++
            schoolCardsList.add(1)
            holder.garbage = schoolCardsList[position]
            notifyDataSetChanged()
        }
        holder.removeSchool.setOnClickListener {
            Log.d(TAG, "numOfSchools: $numOfSchools")
            numOfSchools--
            schoolCardsList.removeAt(position)
            holder.garbage = schoolCardsList[position]
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = numOfSchools
}
