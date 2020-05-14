package com.technion.vedibarta.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.POJOs.Error
import com.technion.vedibarta.POJOs.Loaded
import com.technion.vedibarta.POJOs.SlowLoadingEvent
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.HobbiesAdapter
import com.technion.vedibarta.data.viewModels.HobbiesViewModel
import com.technion.vedibarta.data.viewModels.hobbiesViewModelFactory
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource

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

        viewModel.hobbiesResources.observe(viewLifecycleOwner, Observer {
            when(it){
                is Loaded ->{
                    val hobbyCard = it.data.hobbyCardList
                    val hobbiesResource = it.data.allHobbies
                    val hobbyTitlesList = view.findViewById<RecyclerView>(R.id.hobbyTitlesList)
                    hobbyTitlesList.adapter = HobbiesAdapter(hobbyCard,  viewModel.chosenHobbies, hobbiesResource)
                    hobbyTitlesList.layoutManager = LinearLayoutManager(context)
                }

                is Error -> Toast.makeText(context, it.reason, Toast.LENGTH_LONG).show()
                is SlowLoadingEvent -> {
                    if (!it.handled)
                        Toast.makeText(context, resources.getString(R.string.net_error), Toast.LENGTH_SHORT).show()
                }
            }
        })

        return view
    }

}
