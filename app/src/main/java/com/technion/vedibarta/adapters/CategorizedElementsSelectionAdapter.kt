package com.technion.vedibarta.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.CategoryCardViewModel
import com.technion.vedibarta.databinding.CardCategorizedElementsSelectionBinding
import com.technion.vedibarta.databinding.CardToggleableCategorizedElementsSelectionBinding

class CategorizedElementsSelectionAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModels: List<CategoryCardViewModel>
) : RecyclerView.Adapter<CategorizedElementsSelectionAdapter.CategorizedElementsSelectionViewHolder>() {

    override fun getItemViewType(position: Int) = when (viewModels[position]) {
        is CategoryCardViewModel.Toggleable -> R.layout.card_toggleable_categorized_elements_selection
        is CategoryCardViewModel.NonToggleable -> R.layout.card_categorized_elements_selection
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategorizedElementsSelectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == R.layout.card_toggleable_categorized_elements_selection) {
            val cardBinding = CardToggleableCategorizedElementsSelectionBinding.inflate(inflater, parent, false)
            CategorizedElementsSelectionViewHolder.Toggleable(cardBinding)
        } else {
            val cardBinding = CardCategorizedElementsSelectionBinding.inflate(inflater, parent, false)
            CategorizedElementsSelectionViewHolder.NonToggleable(cardBinding)
        }
    }

    override fun onBindViewHolder(holder: CategorizedElementsSelectionViewHolder, position: Int) {
        when (holder) {
            is CategorizedElementsSelectionViewHolder.Toggleable -> {
                val viewModel = viewModels[position]

                holder.bind(
                    lifecycleOwner,
                     viewModel as CategoryCardViewModel.Toggleable
                )

                viewModel.addOnArrowClickListener {
                    notifyItemChanged(position)
                }
            }
            is CategorizedElementsSelectionViewHolder.NonToggleable ->
                holder.bind(lifecycleOwner, viewModels[position] as CategoryCardViewModel.NonToggleable)
        }
    }

    override fun getItemCount() = viewModels.size

    sealed class CategorizedElementsSelectionViewHolder(view: View): RecyclerView.ViewHolder(view) {

        class Toggleable(
            private val binding: CardToggleableCategorizedElementsSelectionBinding
        ): CategorizedElementsSelectionViewHolder(binding.root) {

            fun bind(lifecycleOwner: LifecycleOwner, viewModel: CategoryCardViewModel.Toggleable) {
                binding.viewModel = viewModel
                binding.lifecycleOwner = lifecycleOwner
                binding.executePendingBindings()

                val recyclerView = binding.bubblesRecycleView
                recyclerView.adapter = BubblesSelectionAdapter(
                    lifecycleOwner,
                    viewModel.bubbleViewModels,
                    viewModel.showBackground
                )
                recyclerView.layoutManager = GridLayoutManager(itemView.context, 3)
            }

        }

        class NonToggleable(
            private val binding: CardCategorizedElementsSelectionBinding
        ): CategorizedElementsSelectionViewHolder(binding.root) {

            fun bind(lifecycleOwner: LifecycleOwner, viewModel: CategoryCardViewModel.NonToggleable) {
                binding.viewModel = viewModel
                binding.lifecycleOwner = lifecycleOwner
                binding.executePendingBindings()

                val recyclerView = binding.bubblesRecycleView
                recyclerView.adapter = BubblesSelectionAdapter(
                    lifecycleOwner,
                    viewModel.bubbleViewModels,
                    viewModel.showBackground
                )
                recyclerView.layoutManager = GridLayoutManager(itemView.context, 3)
            }
        }
    }
}
