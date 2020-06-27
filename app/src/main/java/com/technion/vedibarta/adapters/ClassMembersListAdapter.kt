package com.technion.vedibarta.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.POJOs.Teacher
import com.technion.vedibarta.R

class ClassMembersListAdapter(
    private val teacher: Teacher,
    private val membersList: List<Student>
) :
    RecyclerView.Adapter<ClassMembersListAdapter.MembersViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersViewHolder {
        val schoolAddButtonView = LayoutInflater.from(parent.context)
            .inflate(R.layout.class_member_card, parent, false)
        return MembersViewHolder(schoolAddButtonView, teacher, membersList, parent.context)

    }

    override fun onBindViewHolder(holder: MembersViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = membersList.size + 1

    class MembersViewHolder(
        itemView: View,
        private val teacher: Teacher,
        private val membersList: List<Student>,
        private val context: Context
    ) : RecyclerView.ViewHolder(itemView) {

        private fun displayDefaultProfilePicture(gender: Gender){
            if (gender == Gender.MALE)
                itemView.findViewById<AppCompatImageView>(R.id.memberPhoto)
                    .setImageResource(R.drawable.ic_photo_default_profile_man)
            else
                itemView.findViewById<AppCompatImageView>(R.id.memberPhoto)
                    .setImageResource(R.drawable.ic_photo_default_profile_girl)
        }

        fun bind(position: Int) {
            val photo = itemView.findViewById<AppCompatImageView>(R.id.memberPhoto)

            if (position == 0) {
                if (teacher.photo == null)
                    displayDefaultProfilePicture(teacher.gender)
                else
                    Glide.with(context.applicationContext)
                        .asBitmap()
                        .load(teacher.photo)
                        .apply(RequestOptions.circleCropTransform())
                        .into(photo)
                itemView.findViewById<TextView>(R.id.name).text = teacher.name
                itemView.findViewById<AppCompatImageView>(R.id.rank).visibility = View.VISIBLE
            } else {
                itemView.findViewById<TextView>(R.id.name).text = membersList[position - 1].name
                if (membersList[position - 1].photo == null)
                    displayDefaultProfilePicture(membersList[position - 1].gender)
                else
                    Glide.with(context.applicationContext)
                        .asBitmap()
                        .load(membersList[position - 1].photo)
                        .apply(RequestOptions.circleCropTransform())
                        .into(photo)
            }
        }
    }

}