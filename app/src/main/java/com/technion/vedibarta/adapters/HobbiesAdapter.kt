package com.technion.vedibarta.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.POJOs.CategoryCard
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource

class HobbiesAdapter(
    private val categoryCardList: List<CategoryCard>,
    val chosenHobbies: MutableList<String>,
    private val hobbiesResource: MultilingualResource
) :
    RecyclerView.Adapter<HobbiesAdapter.HobbyCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HobbyCardViewHolder {
        val hobbyCardView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.hobbies_list_element, parent, false)
        return HobbyCardViewHolder(hobbyCardView)
    }

    override fun getItemCount(): Int {
        return categoryCardList.size
    }

    override fun onBindViewHolder(holder: HobbyCardViewHolder, position: Int) {
        holder.bind(categoryCardList[holder.adapterPosition], chosenHobbies, hobbiesResource)
    }

    class HobbyCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var header: TextView
        lateinit var table: TableLayout

        fun bind(
            categoryCard: CategoryCard,
            chosenHobbies: MutableList<String>,
            hobbiesResource: MultilingualResource
        ) {
            header = itemView.findViewById(R.id.hobbyCardTitle)
            table = itemView.findViewById(R.id.hobbiesTables)
            table.removeAllViews()

            header.text = categoryCard.title
            VedibartaFragment.populateHobbiesTable(
                itemView.context,
                table,
                categoryCard.values,
                chosenHobbies,
                hobbiesResource
            )

        }
    }
}