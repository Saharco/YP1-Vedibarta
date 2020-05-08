package com.technion.vedibarta.fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.POJOs.Loaded
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.HobbiesAdapter
import com.technion.vedibarta.data.viewModels.HobbiesViewModel
import com.technion.vedibarta.data.viewModels.hobbiesViewModelFactory
import com.technion.vedibarta.data.viewModels.userSetupViewModelFactory
import com.technion.vedibarta.utilities.VedibartaFragment

/**
 * A simple [Fragment] subclass.
 */
class HobbiesFragment : VedibartaFragment() {

    private val TAG = "HobbiesFragment"

    private val viewModel: HobbiesViewModel by activityViewModels {
        hobbiesViewModelFactory(
            requireActivity().applicationContext
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hobbies, container, false)

        viewModel.resourcesMediator.observe(viewLifecycleOwner, Observer {
            if(it){
                val hobbyCard = viewModel.hobbyCardList.value as Loaded
                val hobbiesResource = viewModel.hobbiesResource.value as Loaded
                val hobbyTitlesList = view.findViewById<RecyclerView>(R.id.hobbyTitlesList)
                hobbyTitlesList.adapter = HobbiesAdapter(hobbyCard.data,  viewModel.chosenHobbies, hobbiesResource.data)
                hobbyTitlesList.layoutManager = LinearLayoutManager(context)
                viewModel.resourcesMediator.removeObservers(viewLifecycleOwner)
            }
        })

        return view
    }

}
