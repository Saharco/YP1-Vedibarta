package com.technion.vedibarta.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView

import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.R

class ClassMembersListAdapter(
    private val teacher: String = "דוד גרוגדטיין",
    private val membersList: MutableList<String> =
        mutableListOf("מרים מיכאלי", "אורית לוי", "שאול כהן", "אנה ינובסקי")
) :
    RecyclerView.Adapter<ClassMembersListAdapter.MembersViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersViewHolder {
        val schoolAddButtonView = LayoutInflater.from(parent.context)
            .inflate(R.layout.class_member_card, parent, false)
        return MembersViewHolder(schoolAddButtonView, teacher, membersList)

    }

    override fun onBindViewHolder(holder: MembersViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = membersList.size+1

    class MembersViewHolder(
        itemView: View,
        private val teacher: String,
        private val membersList: MutableList<String>
    ) : RecyclerView.ViewHolder(itemView) {
        //TODO change to actual type


        fun bind(position: Int) {
            if (position == 0) {
                itemView.findViewById<TextView>(R.id.name).text = teacher
                itemView.findViewById<AppCompatImageView>(R.id.rank).visibility = View.VISIBLE
            } else {
                itemView.findViewById<TextView>(R.id.name).text = membersList[position - 1]
            }
        }
    }

}