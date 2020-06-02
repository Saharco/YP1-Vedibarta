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

class CategorizedBubblesSelectionFragment : Fragment() {

    companion object {
        fun newInstance(identifier: String) = CategorizedBubblesSelectionFragment().apply {
            arguments = Bundle().apply {
                putString("IDENTIFIER", identifier)
            }
        }
    }

    private val identifier by lazy { arguments?.getString("IDENTIFIER")!! }
    private val args by lazy { (requireContext() as ArgumentsSupplier).getCategorizedBubblesSelectionArguments(identifier) }

    private val viewModel: CategorizedBubblesSelectionViewModel by viewModels {
        CategorizedBubblesSelectionViewModelFactory(
            args.translator,
            args.cards,
            args.chosenInitially
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
            args.onSelectedBubblesChangedListener(it)
        }

        val recycler = binding.categoryCardsList

        recycler.adapter = CategorizedElementsSelectionAdapter(viewLifecycleOwner, viewModel.categoryCardViewModels)
        recycler.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    data class Arguments(
        val translator: LiveData<MultilingualTextResource>,
        val cards: List<CategoryCard>,
        val onSelectedBubblesChangedListener: (Set<String>) -> Unit,
        val chosenInitially: Set<String> = emptySet()
    )

    interface ArgumentsSupplier {
        fun getCategorizedBubblesSelectionArguments(identifier: String): Arguments
    }
}