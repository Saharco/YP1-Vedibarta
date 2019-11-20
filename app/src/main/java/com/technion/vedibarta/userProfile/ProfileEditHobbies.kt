package com.technion.vedibarta.userProfile


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.technion.vedibarta.R

/**
 * A simple [Fragment] subclass.
 */
class ProfileEditHobbies : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_edit_hobbies, container, false)
    }


}
