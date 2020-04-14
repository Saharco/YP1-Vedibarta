package com.technion.vedibarta.userProfile


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.HobbiesAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment

/**
 * A simple [Fragment] subclass.
 */
class ProfileEditHobbiesFragment : VedibartaFragment() {

    private val TAG = "HobbiesFragment@Edit"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_edit_hobbies, container, false)
        val hobbyCards = loadHobbies(this.context!!)
        val hobbyTitlesList = view.findViewById(R.id.hobbyTitlesList) as RecyclerView
        hobbyTitlesList.adapter = HobbiesAdapter(hobbyCards, VedibartaActivity.student!!)
        hobbyTitlesList.layoutManager = LinearLayoutManager(this.context)
        return view
    }

}
