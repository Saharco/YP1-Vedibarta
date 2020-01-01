package com.technion.vedibarta.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.POJOs.HobbyCard
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaFragment

class HobbiesAdapter(private val hobbyCard: List<HobbyCard>, val student: Student) :
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
        holder.bind(hobbyCard[position], student)
    }

    class HobbyCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var header: TextView
        lateinit var table: TableLayout

        fun bind(
            hobbyCard: HobbyCard,
            student: Student
        ) {
            header = itemView.findViewById(R.id.hobbyCardTitle)
            table = itemView.findViewById(R.id.hobbiesTables)

            header.text = hobbyCard.title
            VedibartaFragment.populateHobbiesTable(
                itemView.context,
                table,
                hobbyCard.hobbies,
                student
            )

            //TODO: populate the photos of the bubble in the hobbies list with hobbyCard.hobbies
            //TODO: attach listener to each hobby in the hobbies list

        }
    }
}