package com.technion.vedibarta.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.POJOs.Error
import com.technion.vedibarta.POJOs.LoadableData
import com.technion.vedibarta.POJOs.Loaded
import com.technion.vedibarta.POJOs.SlowLoadingEvent
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.UserSetupViewModel
import com.technion.vedibarta.data.viewModels.userSetupViewModelFactory
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource
import kotlinx.android.synthetic.main.fragment_choose_characteristics.*
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
        viewModel.resourcesMediator.observe(viewLifecycleOwner, Observer { observerHandler(it) })
        val mediator = onGenderChangeInit()

        mediator.observe(viewLifecycleOwner, Observer {
            if (it.first && it.second && it.third) {
                val resource = viewModel.characteristics.value as Loaded
                val characteristics = viewModel.characteristicsByCategory.value as Loaded
                loadCharacteristics(
                    characteristics.data[category]
                        ?: emptyArray(), resource.data
                )
                mediator.value = Triple(false, second = false, third = false)
            }
        })

    }

    private fun onGenderChangeInit(): MediatorLiveData<Triple<Boolean, Boolean, Boolean>> {
        val genderChange = MediatorLiveData<Triple<Boolean, Boolean, Boolean>>()
        genderChange.addSource(viewModel.gender) {
            genderChange.value = Triple(true, second = false, third = false)
        }
        genderChange.addSource(viewModel.characteristics) {
            genderChange.value =
                Triple(genderChange.value!!.first, true, genderChange.value!!.third)
        }
        genderChange.addSource(viewModel.characteristicsByCategory) {
            genderChange.value =
                Triple(genderChange.value!!.first, genderChange.value!!.second, true)
        }
        return genderChange
    }

    private fun observerHandler(value: LoadableData<Unit>) {
        when (value) {
            is Loaded -> {
                val resource = (viewModel.characteristics.value as Loaded).data
                val characteristics = (viewModel.characteristicsByCategory.value as Loaded).data
                characteristicsTitle.text = category
                loadCharacteristics(
                    characteristics[category] ?: emptyArray(), resource
                )
                viewModel.resourcesMediator.removeObservers(viewLifecycleOwner)
            }
        }
    }

    private fun loadCharacteristics(
        characteristics: Array<String>,
        resource: MultilingualResource
    ) {
        characteristicsTable.removeAllViews()
        populateCharacteristicsTable(
            requireContext(),
            characteristicsTable,
            characteristics.toList().shuffled(Random(42)).toTypedArray(),
            viewModel.chosenCharacteristics,
            resource
        )
    }

}
