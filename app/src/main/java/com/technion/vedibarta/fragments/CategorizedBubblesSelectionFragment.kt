package com.technion.vedibarta.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.technion.vedibarta.POJOs.CategoryCard
import com.technion.vedibarta.adapters.CategorizedElementsSelectionAdapter
import com.technion.vedibarta.data.viewModels.CategorizedBubblesSelectionViewModel
import com.technion.vedibarta.data.viewModels.CategorizedBubblesSelectionViewModelFactory
import com.technion.vedibarta.databinding.FragmentCategorizedElementsSelectionBinding
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource

abstract class CategorizedBubblesSelectionFragment : Fragment() {

    protected abstract val translator: LiveData<MultilingualTextResource>
    protected abstract val cards: List<CategoryCard>
    protected open val chosenInitially: Set<String> = emptySet()

    protected abstract fun onSelectedBubblesChanged(selected: Set<String>)

    private val viewModel: CategorizedBubblesSelectionViewModel by viewModels {
        CategorizedBubblesSelectionViewModelFactory(
            translator,
            cards,
            chosenInitially
        )
    }

    private var _binding: FragmentCategorizedElementsSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCategorizedElementsSelectionBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.selectedBubbles.observe(viewLifecycleOwner) {
            onSelectedBubblesChanged(it)
        }

        val recycler = binding.categoryCardsList

        recycler.adapter = CategorizedElementsSelectionAdapter(viewLifecycleOwner, viewModel.categoryCardViewModels)
        recycler.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}