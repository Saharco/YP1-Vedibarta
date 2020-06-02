package com.technion.vedibarta.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import com.technion.vedibarta.POJOs.Bubble
import com.technion.vedibarta.adapters.BubblesSelectionAdapter
import com.technion.vedibarta.data.viewModels.*
import com.technion.vedibarta.databinding.FragmentBubblesSelectionBinding
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource

/**
 * A simple [Fragment] subclass.
 */
class BubblesSelectionFragment : VedibartaFragment() {

    companion object {
        fun newInstance(identifier: String) = BubblesSelectionFragment().apply {
            arguments = Bundle().apply {
                putString("IDENTIFIER", identifier)
            }
        }
    }

    private val identifier by lazy { arguments?.getString("IDENTIFIER")!! }
    private val args by lazy { (requireContext() as ArgumentsSupplier).getBubblesSelectionArguments(identifier) }

    private val viewModel: BubblesSelectionViewModel by viewModels {
        BubblesSelectionViewModelFactory(
            args.translator,
            args.title,
            args.bubbles,
            args.selectedInitially
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
            args.onSelectedBubblesChangedListener(it)
        }

        binding.viewModel = viewModel

        val recycler = binding.bubblesRecycleView

        recycler.adapter = BubblesSelectionAdapter(viewLifecycleOwner, viewModel.bubbleViewModels, args.showBackground)
        recycler.layoutManager = GridLayoutManager(requireContext(), 3)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    data class Arguments(
        val translator: LiveData<MultilingualTextResource>,
        val title: String,
        val bubbles: List<Bubble>,
        val onSelectedBubblesChangedListener: (Set<String>) -> Unit,
        val showBackground: Boolean,
        val selectedInitially: Set<String> = emptySet()
    )

    interface ArgumentsSupplier {
        fun getBubblesSelectionArguments(identifier: String): Arguments
    }
}
