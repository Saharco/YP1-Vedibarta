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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.technion.vedibarta.POJOs.LoadableData
import com.technion.vedibarta.POJOs.Loaded
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.UserSetupResources
import com.technion.vedibarta.data.viewModels.UserSetupViewModel
import com.technion.vedibarta.data.viewModels.userSetupViewModelFactory
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import kotlinx.android.synthetic.main.fragment_choose_characteristics.*
import java.lang.ClassCastException
import kotlin.random.Random

/**
 * A simple [Fragment] subclass.
 */
class ChooseCharacteristicsFragment(private var category: String = "") : VedibartaFragment() {

    private val TAG = "CharFragment@Setup"

    private val viewModel: UserSetupViewModel by activityViewModels {
        userSetupViewModelFactory(
            requireActivity().applicationContext
        )
    }

    private lateinit var onCharacteristicClick: OnCharacteristicClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onCharacteristicClick = context as? OnCharacteristicClickListener
            ?: throw ClassCastException("$context must implement ${OnCharacteristicClickListener::class}")
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
        outState.putString("category", category)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        category = savedInstanceState?.getString("category") ?: category
        viewModel.userSetupResources.observe(viewLifecycleOwner, Observer { observerHandler(it) })
    }

    private fun observerHandler(value: LoadableData<UserSetupResources>) {
        if (value is Loaded) {
            val resource = value.data.allCharacteristics
            val characteristics = value.data.characteristicsByCategory

            characteristicsTitle.text = category
            loadCharacteristics(characteristics[category] ?: emptyArray(), resource)

        }
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
            viewModel.chosenCharacteristics,
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
                                viewModel.chosenCharacteristics,
                                resource
                            )
                            onCharacteristicClick.onCharacteristicClick(category)
                        }
                }
            }
        }
    }

}

interface OnCharacteristicClickListener {
    fun onCharacteristicClick(category: String)
}
