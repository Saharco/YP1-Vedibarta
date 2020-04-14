package com.technion.vedibarta.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.HobbiesAdapter
import com.technion.vedibarta.utilities.VedibartaFragment

/**
 * A simple [Fragment] subclass.
 */
class ChooseHobbiesFragment : VedibartaFragment() {

    private val TAG = "HobbiesFragment@login"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_choose_hobbies, container, false)
        val hobbyCards = loadHobbies(this.context!!)
        val hobbyTitlesList = view.findViewById(R.id.hobbyTitlesList) as RecyclerView
        hobbyTitlesList.adapter = HobbiesAdapter(hobbyCards, (activity as UserSetupActivity).setupStudent)
        hobbyTitlesList.layoutManager = LinearLayoutManager(this.context)

        return view
    }




}
