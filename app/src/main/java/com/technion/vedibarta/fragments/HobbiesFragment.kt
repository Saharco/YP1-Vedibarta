package com.technion.vedibarta.fragments


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.HobbiesAdapter
import com.technion.vedibarta.data.viewModels.Loaded
import com.technion.vedibarta.data.viewModels.UserSetupViewModel
import com.technion.vedibarta.data.viewModels.userSetupViewModelFactory
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource

/**
 * A simple [Fragment] subclass.
 */
class HobbiesFragment : VedibartaFragment() {

    private val TAG = "HobbiesFragment"

    private lateinit var argumentTransfer: ArgumentTransfer

    private val viewModel: UserSetupViewModel by activityViewModels {
        userSetupViewModelFactory(
            requireActivity().applicationContext
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        argumentTransfer = context as? ArgumentTransfer ?:
                throw ClassCastException("$context must implement ${ArgumentTransfer::class}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hobbies, container, false)

//        val argMap = argumentTransfer.getArgs()
//        val hobbiesResourceTask = argMap["hobbiesResourceTask"] as Task<MultilingualResource>
//        val hobbyCardTask = argMap["hobbyCardTask"] as Task<List<HobbyCard>>
//        val student = argMap["student"] as Student
//        val act = argMap["activity"] as Activity

        viewModel.resourcesMediator.observe(viewLifecycleOwner, Observer {
            val hobbyCard = viewModel.hobbyCardList.value as Loaded
            val hobbiesResource = viewModel.hobbiesResource.value as Loaded
            if(it){
                val hobbyTitlesList = view.findViewById<RecyclerView>(R.id.hobbyTitlesList)
                hobbyTitlesList.adapter = HobbiesAdapter(hobbyCard.data,  viewModel.chosenHobbies, hobbiesResource.data)
                hobbyTitlesList.layoutManager = LinearLayoutManager(context)
            }
        })

//        Tasks.whenAll(hobbiesResourceTask, hobbyCardTask)
//            .addOnSuccessListener(act) {
//                val hobbyTitlesList = view.findViewById<RecyclerView>(R.id.hobbyTitlesList)
//                hobbyTitlesList.adapter = HobbiesAdapter(hobbyCardTask.result!!,  student, hobbiesResourceTask.result!!)
//                hobbyTitlesList.layoutManager = LinearLayoutManager(context)
//            }

        return view
    }

}
