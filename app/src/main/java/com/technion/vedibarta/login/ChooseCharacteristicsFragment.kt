package com.technion.vedibarta.login


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.Loaded
import com.technion.vedibarta.data.viewModels.UserSetupViewModel
import com.technion.vedibarta.data.viewModels.userSetupViewModelFactory
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import kotlinx.android.synthetic.main.fragment_choose_characteristics.*
import kotlin.random.Random


/**
 * A simple [Fragment] subclass.
 */
class ChooseCharacteristicsFragment : VedibartaFragment(), UserSetupActivity.OnNextClickForCharacteristics {

    private val TAG = "CharFragment@Setup"
    private lateinit var argumentTransfer: ArgumentTransfer

    private var currentIndex = 0

    private val viewModel: UserSetupViewModel by activityViewModels {
        userSetupViewModelFactory(
            requireActivity().applicationContext
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        argumentTransfer = context as? ArgumentTransfer
            ?: throw ClassCastException("$context must implement ${ArgumentTransfer::class}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose_characteristics, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentIndex = 0

        viewModel.resourcesMediator.observe(viewLifecycleOwner, Observer {
            val resource = (viewModel.characteristics.value as Loaded).data
            val characteristics = (viewModel.characteristicsByCategory.value as Loaded).data
            characteristicsTitle.text = characteristics.keys.toList().first()
            loadCharacteristics(characteristics[characteristics.keys.toList().first()]?: emptyArray(), resource)
        })

        //TODO add observe for gender change

    }

    private fun loadCharacteristics(
        characteristics: Array<String>,
        resource: MultilingualTextResource
    ) {
        characteristicsTable.removeAllViews()
        populateCharacteristicsTable(requireContext(), characteristicsTable, characteristics.toList().shuffled(Random(42)).toTypedArray(), viewModel.chosenCharacteristics, resource)
    }

    override fun onNextClick(): Boolean {
        val resource = viewModel.characteristics.value as Loaded
        val characteristics = viewModel.characteristicsByCategory.value as Loaded
        if (currentIndex < characteristics.data.keys.size-1){
            currentIndex++
            characteristicsTitle.text = characteristics.data.keys.toList()[currentIndex]
            loadCharacteristics(characteristics.data[characteristics.data.keys.toList()[currentIndex]]?: emptyArray(), resource.data)
            return false
        }
        return true
    }

    override fun onBackClick(): Boolean {
        val resource = viewModel.characteristics.value as Loaded
        val characteristics = viewModel.characteristicsByCategory.value as Loaded
        if (currentIndex > 0){
            currentIndex--
            characteristicsTitle.text = characteristics.data.keys.toList()[currentIndex]
            loadCharacteristics(characteristics.data[characteristics.data.keys.toList()[currentIndex]]?: emptyArray(), resource.data)
            return false
        }
        return true
    }
}
