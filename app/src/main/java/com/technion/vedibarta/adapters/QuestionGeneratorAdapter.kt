package com.technion.vedibarta.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.R



class QuestionGeneratorCategoryAdapter(private val categoryList: Array<String>, private val onClick: (String)->Unit = {}) : RecyclerView.Adapter<QuestionGeneratorCategoryAdapter.CategoryViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {
        val categoryView = LayoutInflater.from(parent.context)
            .inflate(R.layout.question_categories_list_element, parent, false)
        val lp = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        categoryView.layoutParams = lp
        return CategoryViewHolder(categoryView)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int
    ) {
        holder.bind(categoryList[holder.adapterPosition], onClick)
    }


    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(category: String, onClick: (String) -> Unit){
            val header :TextView= itemView.findViewById(R.id.categoryText)
            header.text = category
            header.width = View.MeasureSpec.EXACTLY
            header.setOnClickListener { onClick(category) }
        }
    }
}

