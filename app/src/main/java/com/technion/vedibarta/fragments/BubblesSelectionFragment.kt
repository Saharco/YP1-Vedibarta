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

/**
 * The base class to all fragments that display bubbles and allow them to be selected.
 *
 * This class is implemented using the Inversion of Control design pattern. Extending fragment
 * classes have to implement the following properties:
 *  @property translator a [LiveData] which will be used to translate the [categoryCard] info
 *   (title and bubbles' content) to the current language
 *  @property categoryCard the [CategoryCard] which content will be displayed. Its info (title and
 *   bubble's content) shall be saved in the base language of the [translator]
 *  @see onSelectedBubblesChanged
 * And optionally, override the following property:
 *  @property chosenInitially the set of bubbles that should be displayed as chosen when the
 *   fragment is created (identified by their content).
 *
 * [translator], [categoryCard] and [chosenInitially] are guaranteed to only be observed after
 * [onAttach] is called.
 */
abstract class BubblesSelectionFragment : VedibartaFragment() {

    protected abstract val translator: LiveData<out MultilingualTextResource>
    protected abstract val categoryCard: CategoryCard
    protected abstract val chosenInitially: Set<String>

    /**
     * A callback to be called whenever the set of selected bubbles is changed.
     *
     * @param selected a set containing the new selected bubbles (identified by their content)
     */
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
