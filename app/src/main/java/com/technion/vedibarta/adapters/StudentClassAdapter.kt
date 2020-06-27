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
import com.google.android.material.card.MaterialCardView
import com.technion.vedibarta.POJOs.Class
import com.technion.vedibarta.R

class StudentClassAdapter(
    private val classPressLambda: (View) -> Unit,
    private val classesList: MutableList<Class>
) : RecyclerView.Adapter<StudentClassAdapter.ClassesCardViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClassesCardViewHolder {
        val schoolCardView =
            LayoutInflater.from(parent.context).inflate(R.layout.class_card, parent, false)
        return ClassesCardViewHolder(parent.context, schoolCardView, classPressLambda)
    }

    override fun getItemCount(): Int = classesList.size


    override fun onBindViewHolder(holder: ClassesCardViewHolder, position: Int) {
        holder.bind(classesList[position])
    }

    class ClassesCardViewHolder(
        private val context: Context,
        itemView: View,
        private val classPressLambda: (View) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(cls: Class) {
            itemView.findViewById<MaterialCardView>(R.id.classRoot).isChecked = false
            itemView.findViewById<TextView>(R.id.className).text = cls.name
            itemView.findViewById<TextView>(R.id.classDescription).text = cls.description
            itemView.findViewById<AppCompatImageView>(R.id.shareGroupButton).visibility = View.GONE
            itemView.tag = cls.id
            itemView.setOnClickListener {
                classPressLambda(it)
            }

            if (cls.photo == null)
                itemView.findViewById<AppCompatImageView>(R.id.classPhoto)
                    .setImageResource(R.drawable.ic_class_default_photo)
            else
                Glide.with(context.applicationContext)
                    .asBitmap()
                    .load(cls.photo)
                    .apply(RequestOptions.circleCropTransform())
                    .into(itemView.findViewById<AppCompatImageView>(R.id.classPhoto))
        }
    }




}