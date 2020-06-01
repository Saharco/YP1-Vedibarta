package com.technion.vedibarta.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.data.viewModels.BubbleViewModel
import com.technion.vedibarta.databinding.BubbleWithBackgroundBinding
import com.technion.vedibarta.databinding.BubbleWithoutBackgroundBinding

class BubblesSelectionAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val bubbleViewModels: List<BubbleViewModel>,
    private val showBackground: Boolean
) : RecyclerView.Adapter<BubblesSelectionAdapter.BubbleViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BubbleViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (showBackground) {
            val binding = BubbleWithBackgroundBinding.inflate(inflater, parent, false)
            BubbleViewHolder.BubbleWithBackgroundViewHolder(binding)
        } else {
            val binding = BubbleWithoutBackgroundBinding.inflate(inflater, parent, false)
            BubbleViewHolder.BubbleWithoutBackgroundViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: BubbleViewHolder, position: Int) =
        holder.bind(lifecycleOwner, bubbleViewModels[position])

    override fun getItemCount() = bubbleViewModels.size

    sealed class BubbleViewHolder(view: View): RecyclerView.ViewHolder(view) {

        abstract fun bind(lifecycleOwner: LifecycleOwner, viewModel: BubbleViewModel)

        class BubbleWithBackgroundViewHolder(
            private val binding: BubbleWithBackgroundBinding
        ) : BubbleViewHolder(binding.root) {

            override fun bind(lifecycleOwner: LifecycleOwner, viewModel: BubbleViewModel) {
                binding.viewModel = viewModel
                binding.lifecycleOwner = lifecycleOwner
                binding.executePendingBindings()
            }
        }

        class BubbleWithoutBackgroundViewHolder(
            private val binding: BubbleWithoutBackgroundBinding
        ) : BubbleViewHolder(binding.root) {

            override fun bind(lifecycleOwner: LifecycleOwner, viewModel: BubbleViewModel) {
                binding.viewModel = viewModel
                binding.lifecycleOwner = lifecycleOwner
                binding.executePendingBindings()
            }
        }
    }
}