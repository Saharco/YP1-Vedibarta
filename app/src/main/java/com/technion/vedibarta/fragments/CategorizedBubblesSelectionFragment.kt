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

/**
 * The base class to all fragments that display categorized bubbles and allow them to be selected.
 *
 * This class is implemented using the Inversion of Control design pattern. Extending fragment
 * classes have to implement the following properties:
 *  @property translator a [LiveData] which will be used to translate the [cards] info (title and
 *   bubbles' content) to the current language
 *  @property cards a list of [CategoryCard] to be displayed. Their info (title and bubble's
 *   content) shall be saved in the base language of the [translator]
 *  @see onSelectedBubblesChanged
 * And optionally, override the following property:
 *  @property chosenInitially the set of bubbles that should be displayed as chosen when the
 *   fragment is created (identified by their content).
 *
 * [translator], [cards] and [chosenInitially] are guaranteed to only be observed after [onAttach]
 * is called.
 */
abstract class CategorizedBubblesSelectionFragment : Fragment() {

    protected abstract val translator: LiveData<MultilingualTextResource>
    protected abstract val cards: List<CategoryCard>
    protected open val chosenInitially: Set<String> = emptySet()

    /**
     * A callback to be called whenever the set of selected bubbles is changed.
     *
     * @param selected a set containing the new selected bubbles (identified by their content)
     */
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