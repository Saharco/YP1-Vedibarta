package com.technion.vedibarta.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.POJOs.Error
import com.technion.vedibarta.POJOs.Loaded
import com.technion.vedibarta.POJOs.SlowLoadingEvent

import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.CharacteristicsAdapter
import com.technion.vedibarta.data.viewModels.CharacteristicsViewModel
import com.technion.vedibarta.data.viewModels.characteristicsViewModelFactory
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment

class CharacteristicsFragment : VedibartaFragment() {

    private val TAG = "CharacteristicsFragment"
    private val viewModel: CharacteristicsViewModel by activityViewModels {
        characteristicsViewModelFactory(
            requireActivity().applicationContext,
            VedibartaActivity.student!!.gender
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_characteristics, container,
            false
        )
        viewModel.characteristicsResources.observe(viewLifecycleOwner, Observer {
            when(it){
                is Loaded ->{
                    val characteristicsCardList = it.data.characteristicsCardList
                    val characteristicsResource = it.data.allCharacteristics
                    val characteristicTitlesList = view.findViewById<RecyclerView>(R.id.characteristicsTitlesList)
                    characteristicTitlesList.adapter = CharacteristicsAdapter(characteristicsCardList,  viewModel.chosenCharacteristics, characteristicsResource)
                    characteristicTitlesList.layoutManager = LinearLayoutManager(context)
                }


            }
        })
        return view
    }

}
