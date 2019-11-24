package com.technion.vedibarta.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.carousel_item.view.*

class CarouselAdapter(val itemClick: (position:Int, item: Item) -> Unit) : RecyclerView.Adapter<ItemViewHolder>() {

    private var items: List<Item> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.carousel_item, parent, false))

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            itemClick(position,items[position])
        }
    }

    override fun getItemCount() = items.size

    fun setItems(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}

class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bind(item: Item) {
        //TODO: bind the views here
    }
}

data class Item(
    val title: String,
    @DrawableRes val icon: Int
)