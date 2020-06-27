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

class ClassesListAdapter(
    private val addButtonLambda: () -> Unit,
    private val longPressLambda: (v: View) -> Boolean,
    private val classPressLambda: (v: View) -> Boolean,
    private val onSharePressLambda: (View) -> Unit,
    private val classesList: MutableList<Class>
) : RecyclerView.Adapter<ClassesListAdapter.ClassesViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClassesViewHolder {
        return if (viewType == 0) {
            val schoolAddButtonView = LayoutInflater.from(parent.context)
                .inflate(R.layout.add_school_card, parent, false)
            ClassesViewHolder.AddClassViewHolder(schoolAddButtonView, parent, addButtonLambda)

        } else {
            val schoolCardView =
                LayoutInflater.from(parent.context).inflate(R.layout.class_card, parent, false)
            ClassesViewHolder.ClassesCardViewHolder(
                parent.context,
                schoolCardView,
                classPressLambda,
                longPressLambda,
                onSharePressLambda
            )
        }
    }

    override fun getItemCount(): Int = classesList.size + 1

    override fun onBindViewHolder(holder: ClassesViewHolder, position: Int) {
        when (holder) {
            is ClassesViewHolder.AddClassViewHolder -> holder.bind()
            is ClassesViewHolder.ClassesCardViewHolder -> holder.bind(classesList[position - 1])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position > 0) position else 0
    }

    sealed class ClassesViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        class AddClassViewHolder(
            itemView: View,
            private val parent: ViewGroup,
            private val addButtonLambda: () -> Unit
        ) :
            ClassesViewHolder(itemView) {
            fun bind() {
                val addClassButton = itemView.findViewById<TextView>(R.id.addSchoolButton)
                addClassButton.text = parent.context.getString(R.string.teacher_add_class)
                addClassButton.setOnClickListener {
                    addButtonLambda()
                }
            }
        }

        class ClassesCardViewHolder(
            private val context: Context,
            itemView: View,
            private val classPressLambda: (v: View) -> Boolean,
            private val longPressLambda: (v: View) -> Boolean,
            private val onSharePressLambda: (View) -> Unit
        ) : ClassesViewHolder(itemView) {
            fun bind(cls: Class) {
                itemView.findViewById<MaterialCardView>(R.id.classRoot).isChecked = false
                itemView.findViewById<TextView>(R.id.className).text = cls.name
                itemView.findViewById<TextView>(R.id.classDescription).text = cls.description
                itemView.tag = cls.id
                itemView.setOnLongClickListener {
                    longPressLambda(it)
                }
                itemView.setOnClickListener {
                    classPressLambda(it)
                }
                itemView.findViewById<AppCompatImageView>(R.id.shareGroupButton)
                    .setOnClickListener {
                        onSharePressLambda(itemView)
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
}