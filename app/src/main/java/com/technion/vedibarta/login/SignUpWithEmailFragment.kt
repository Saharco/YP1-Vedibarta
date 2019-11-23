package com.technion.vedibarta.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.technion.vedibarta.R
import java.lang.ClassCastException


class SignUpWithEmailFragment : Fragment() {
    private lateinit var backListener: OnBackButtonClickListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_up_with_email, container, false)

        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { back() }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        backListener = context as? OnBackButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnBackButtonClickListener::class}")
    }

    private fun back(){
        backListener.onBackButtonClick()
    }

    interface OnBackButtonClickListener {
        fun onBackButtonClick()
    }
}
