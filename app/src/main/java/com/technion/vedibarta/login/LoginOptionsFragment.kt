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


class LoginOptionsFragment : Fragment() {
    lateinit var signInListener : OnSignInButtonClickListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login_options, container, false)

        val loginButton = view.findViewById<Button>(R.id.sign_in_link)
        loginButton.setOnClickListener { signIn() }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        signInListener = context as? OnSignInButtonClickListener ?:
                throw ClassCastException("$context must implement OnSignInButtonClickListener")
    }

    private fun signIn(){
        signInListener.onSignInButtonClick()
    }

    interface OnSignInButtonClickListener {
        fun onSignInButtonClick()
    }
}