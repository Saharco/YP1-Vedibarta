package com.technion.vedibarta.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import com.technion.vedibarta.POJOs.CategoryCard
import com.technion.vedibarta.adapters.BubblesSelectionAdapter
import com.technion.vedibarta.data.viewModels.*
import com.technion.vedibarta.databinding.FragmentBubblesSelectionBinding
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource

abstract class BubblesSelectionFragment : VedibartaFragment() {

    protected abstract val translator: LiveData<MultilingualTextResource>
    protected abstract val categoryCard: CategoryCard
    protected abstract val chosenInitially: Set<String>

    protected abstract fun onSelectedBubblesChanged(selected: Set<String>)

    private val viewModel: BubblesSelectionViewModel by viewModels {
        BubblesSelectionViewModelFactory(
            translator,
            categoryCard.title,
            categoryCard.bubbles,
            chosenInitially
        )
    }

    private var _binding: FragmentBubblesSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBubblesSelectionBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.selectedBubbles.observe(viewLifecycleOwner) {
            onSelectedBubblesChanged(it)
        }

        binding.viewModel = viewModel

        val recycler = binding.bubblesRecycleView

        recycler.adapter = BubblesSelectionAdapter(
            viewLifecycleOwner,
            viewModel.bubbleViewModels,
            categoryCard.showBackgrounds
        )
        recycler.layoutManager = GridLayoutManager(requireContext(), 3)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
