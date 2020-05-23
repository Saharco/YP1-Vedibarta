package com.technion.vedibarta.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.*
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import kotlinx.android.synthetic.main.fragment_choose_characteristics.*
import kotlin.random.Random

/**
 * A simple [Fragment] subclass.
 */
class ChooseElementsFragment(
    private var title: String = "",
    resource: LiveData<MultilingualTextResource>? = null,
    chosenList: MutableList<String> = mutableListOf()
) : VedibartaFragment() {

    private val TAG = "CharFragment@Setup"

    private val viewModel: ChooseElementsViewModel by viewModels {
        chooseElementsViewModelFactory(
            resource, chosenList
        )
    }

    private var onCharacteristicClick: OnCharacteristicClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onCharacteristicClick = context as? OnCharacteristicClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose_characteristics, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("category", title)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = savedInstanceState?.getString("category") ?: title
        viewModel.resource?.observe(viewLifecycleOwner, Observer { observerHandler(it) })
    }

    private fun observerHandler(resource: MultilingualTextResource) {
        val characteristics = resource.getAll().toTypedArray()

        characteristicsTitle.text = title
        loadCharacteristics(characteristics, resource)

    }

    private fun loadCharacteristics(
        characteristics: Array<String>,
        resource: MultilingualTextResource
    ) {
        characteristicsTable.removeAllViews()
        populateCharacteristicsTable(
            requireContext(),
            characteristicsTable,
            characteristics.toList().shuffled(Random(42)).toTypedArray(),
            viewModel.chosenList,
            resource
        )
        characteristicsTable.forEach { row ->
            if (row is TableRow) {
                row.forEach {
                    if (it is ConstraintLayout)
                        it.setOnClickListener { view ->
                            characteristicsTableItemClickHandler(
                                view,
                                requireContext(),
                                characteristics.toList().shuffled(Random(42)).toTypedArray(),
                                characteristicsTable,
                                viewModel.chosenList,
                                resource
                            )
                            onCharacteristicClick?.onCharacteristicClick(title)
                        }
                }
            }
        }
    }

}

interface OnCharacteristicClickListener {
    fun onCharacteristicClick(category: String)
}
