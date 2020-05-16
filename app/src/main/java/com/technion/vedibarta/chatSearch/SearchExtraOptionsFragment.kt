package com.technion.vedibarta.chatSearch


import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.fragment_search_extra_options.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.data.viewModels.ChatSearchViewModel
import com.technion.vedibarta.data.viewModels.chatSearchViewModelFactory
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.Resource


/**
 * A simple [Fragment] subclass.
 */
class SearchExtraOptionsFragment : VedibartaFragment() {

    private val TAG = "ExtraFragment@Search"

    private val viewModel: ChatSearchViewModel by activityViewModels {
        chatSearchViewModelFactory(
            requireActivity().applicationContext
        )
    }

    private lateinit var schoolAndRegionMap: Map<String, String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_extra_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        combineResources(viewModel.schoolsName, viewModel.regionsName)
            .observe(viewLifecycleOwner, Observer {
                if (it is Loaded)
                    setupAndInitViews(it.data.schoolsName, it.data.regionsName)
            })
    }

    override fun onPause() {
        super.onPause()
        VedibartaActivity.hideKeyboard(activity as ChatSearchActivity)
        regionListSpinner.clearFocus()
        schoolListSpinner.clearFocus()
    }

    private fun onSchoolSelectedListener(position: Int) {
        val schoolName = schoolListSpinner.adapter.getItem(position).toString()
        val region = schoolAndRegionMap[schoolName].toString()

        regionListSpinner.text = SpannableStringBuilder(region)
        viewModel.chosenSchool = Filled(schoolName)
        viewModel.chosenRegion = Filled(region)

        VedibartaActivity.hideKeyboard(requireActivity())
    }

    private fun onRegionSelectedListener(position: Int) {
        schoolListSpinner.text = SpannableStringBuilder("")
        val region = regionListSpinner.adapter.getItem(position).toString()
        viewModel.chosenRegion = Filled(region)
        viewModel.chosenSchool = Filled("")

        val schoolList = schoolAndRegionMap.filter { it.value == region }.keys.toTypedArray()

        populateAutoTextView(requireContext(), schoolListSpinner, schoolList)

        VedibartaActivity.hideKeyboard(requireActivity())
    }

    private fun schoolOnCheckedChanged(isChecked: Boolean) {
        schoolListSpinner.text.clear()
        viewModel.chosenSchool = Unfilled
        VedibartaActivity.hideKeyboard(requireActivity())
        if (isChecked) {
            schoolListSpinner.visibility = View.VISIBLE
        } else {
            schoolListSpinner.visibility = View.GONE
        }
    }

    private fun regionOnCheckedChanged(isChecked: Boolean) {
        VedibartaActivity.hideKeyboard(requireActivity())
        regionListSpinner.text.clear()
        viewModel.chosenRegion = Unfilled
        if (isChecked) {
            regionListSpinner.visibility = View.VISIBLE
        } else {
            regionListSpinner.visibility = View.GONE
        }
    }

    private fun gradeOnCheckChanged(isChecked: Boolean){
        gradeRadioGroup.clearCheck()
        viewModel.grade = Grade.NONE
        if (isChecked)
            gradeRadioGroup.visibility = View.VISIBLE
        else
            gradeRadioGroup.visibility = View.GONE
    }

    private fun setupAndInitViews(
        schoolsName: Resource,
        regionsName: Resource
    ) {
        schoolAndRegionMap = schoolsName.getAll().zip(regionsName.getAll()).toMap()

        //---Switch Views---

        schoolFilterSwitch.setOnCheckedChangeListener { _, isChecked -> schoolOnCheckedChanged(isChecked) }
        regionFilterSwitch.setOnCheckedChangeListener { _, isChecked -> regionOnCheckedChanged(isChecked) }

        //---DropDownList Views---
        schoolListSpinner.setOnItemClickListener { _, _, pos, _ -> onSchoolSelectedListener(pos) }
        regionListSpinner.setOnItemClickListener { _, _, pos, _ -> onRegionSelectedListener(pos) }

        regionListSpinner.doOnTextChanged { text, _, _, _ ->
            populateAutoTextView(
                requireContext(),
                schoolListSpinner,
                schoolsName.getAll().toTypedArray()
            )
            viewModel.chosenRegion = if (text.isNullOrEmpty()) Unfilled else Filled(text.toString())
        }

        schoolListSpinner.doOnTextChanged { text, _, _, _ ->
            viewModel.chosenSchool = if (text.isNullOrEmpty()) Unfilled else Filled(text.toString())
        }

        gradeFilterSwitch.setOnCheckedChangeListener { _, isChecked ->  gradeOnCheckChanged(isChecked)}

        populateAutoTextView(
            requireContext(),
            regionListSpinner,
            regionsName.getAll().distinct().toTypedArray()
        )
        populateAutoTextView(
            requireContext(),
            schoolListSpinner,
            schoolsName.getAll().toTypedArray()
        )
    }

    private fun combineResources(
        schoolsNameLiveData: LiveData<LoadableData<Resource>>,
        regionsNameLiveData: LiveData<LoadableData<Resource>>
    ): LiveData<LoadableData<ExtraOptionsResources>> {
        val mediator = MediatorLiveData<LoadableData<ExtraOptionsResources>>()
            .apply { value = NormalLoading() }

        fun refreshCombination() {
            // cannot go back after reaching an end-state
            if (mediator.value is Error) return

            val schoolsName = schoolsNameLiveData.value
            val regionsName = regionsNameLiveData.value

            // become Loaded when all resources have been loaded
            if (schoolsName is Loaded
                && regionsName is Loaded
            ) {
                mediator.value = Loaded(
                    ExtraOptionsResources(
                        schoolsName = schoolsName.data,
                        regionsName = regionsName.data
                    )
                )
                return
            }

            // become Error when the first error occurs
            schoolsName?.let {
                if (it is Error) {
                    mediator.value = Error(it.reason); return
                }
            }
            regionsName?.let {
                if (it is Error) {
                    mediator.value = Error(it.reason); return
                }
            }

            // trigger SlowLoadingEvent when the first SlowLoadingEvent occurs
            if (mediator.value !is SlowLoadingEvent
                && (schoolsName is SlowLoadingEvent
                        || regionsName is SlowLoadingEvent)
            ) {
                mediator.value = SlowLoadingEvent()
                return
            }
        }

        mediator.addSource(schoolsNameLiveData) { refreshCombination() }
        mediator.addSource(regionsNameLiveData) { refreshCombination() }

        return mediator
    }

    data class ExtraOptionsResources(
        val schoolsName: Resource,
        val regionsName: Resource
    )
}
