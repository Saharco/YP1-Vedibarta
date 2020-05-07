package com.technion.vedibarta.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.POJOs.HobbyCard
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource

class HobbiesAdapter(
    private val hobbyCard: List<HobbyCard>,
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
        return hobbyCard.size
    }

    override fun onBindViewHolder(holder: HobbyCardViewHolder, position: Int) {
        holder.bind(hobbyCard[holder.adapterPosition], chosenHobbies, hobbiesResource)
    }

    class HobbyCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var header: TextView
        lateinit var table: TableLayout

        fun bind(
            hobbyCard: HobbyCard,
            chosenHobbies: MutableList<String>,
            hobbiesResource: MultilingualResource
        ) {
            header = itemView.findViewById(R.id.hobbyCardTitle)
            table = itemView.findViewById(R.id.hobbiesTables)
            table.removeAllViews()

            header.text = hobbyCard.title
            VedibartaFragment.populateHobbiesTable(
                itemView.context,
                table,
                hobbyCard.hobbies,
                chosenHobbies,
                hobbiesResource
            )

        }
    }
}