package com.technion.vedibarta.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.zagum.expandicon.ExpandIconView
import com.technion.vedibarta.POJOs.CategoryCard
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource

class CharacteristicsAdapter(
    private val categoryCardList: List<CategoryCard>,
    private val chosenCharacteristics: MutableMap<String, Boolean>,
    private val characteristicsResource: MultilingualTextResource
) : RecyclerView.Adapter<CharacteristicsAdapter.CharacteristicCardViewHolder>()  {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CharacteristicCardViewHolder {
        val characteristicCardView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.characteristics_list_element, parent, false)
        return CharacteristicCardViewHolder(characteristicCardView)
    }

    override fun getItemCount(): Int {
        return categoryCardList.size
    }

    override fun onBindViewHolder(holder: CharacteristicCardViewHolder, position: Int) {
        holder.bind(categoryCardList[holder.adapterPosition], chosenCharacteristics, characteristicsResource)
    }

    class CharacteristicCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            categoryCard: CategoryCard,
            chosenCharacteristics: MutableMap<String, Boolean>,
            characteristicsResource: MultilingualTextResource
        ) {
            val title: TextView = itemView.findViewById(R.id.characteristicCardTitle)
            val table: TableLayout = itemView.findViewById(R.id.characteristicsTable)
            val arrowButton: ExpandIconView = itemView.findViewById(R.id.arrowButton)
            table.removeAllViews()
            title.text = categoryCard.title

            arrowButton.setOnClickListener {
                if (table.visibility == View.VISIBLE)
                    table.visibility = View.GONE
                else
                    table.visibility = View.VISIBLE
                arrowButton.switchState()
            }
            title.setOnClickListener {
                if (table.visibility == View.VISIBLE)
                    table.visibility = View.GONE
                else
                    table.visibility = View.VISIBLE
                arrowButton.switchState()

            }
            arrowButton.switchState()
            arrowButton.setAnimationDuration(200)
            VedibartaFragment.populateCharacteristicsTable(
                itemView.context,
                table,
                categoryCard.values,
                chosenCharacteristics,
                characteristicsResource
            )
        }
    }
}