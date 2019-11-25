package com.technion.vedibarta.adapters

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.R

class CarouselAdapter(val context: Context, val itemClick: (position: Int, carouselAdapterItem: CarouselAdapterItem) -> Unit) :
    RecyclerView.Adapter<ItemViewHolder>() {

    private var carouselAdapterItems: List<CarouselAdapterItem> = listOf()
    private val selectedPos = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val orientation = context.resources.configuration.orientation
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ItemViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.carousel_item_horizontal,
                    parent,
                    false
                ))
        } else {
            ItemViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.carousel_item,
                    parent,
                    false
                ))
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(carouselAdapterItems[position])
        holder.itemView.setOnClickListener {
            itemClick(position, carouselAdapterItems[position])
        }
        holder.itemView.isSelected = position == selectedPos
    }

    override fun getItemCount() = carouselAdapterItems.size

    fun setItems(newCarouselAdapterItems: List<CarouselAdapterItem>) {
        carouselAdapterItems = newCarouselAdapterItems
        notifyDataSetChanged()
    }
}

class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bind(carouselAdapterItem: CarouselAdapterItem) {
        //TODO: bind the views here
    }
}

data class CarouselAdapterItem(
    val title: String,
    @DrawableRes val icon: Int
)