package com.technion.vedibarta.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.SignUpViewModel
import com.technion.vedibarta.databinding.FragmentSignUpWithEmailBinding
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.hideKeyboard
import com.technion.vedibarta.utilities.isEmail
import kotlinx.android.synthetic.main.fragment_sign_up_with_email.*
import java.lang.ClassCastException

/**
 * The sign-up with email screen.
 *
 * This class handles the actions taken when any button is pressed on the sign-up with email screen.
 *
 * @see R.layout.fragment_sign_up_with_email
 */
class SignUpWithEmailFragment : Fragment() {
    private val viewModel: SignUpViewModel by viewModels()

    private lateinit var backListener: OnBackButtonClickListener
    private lateinit var signUpListener: OnSignUpButtonClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)

        backListener = context as? OnBackButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnBackButtonClickListener::class}")
        signUpListener = context as? OnSignUpButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnSignUpButtonClickListener::class}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSignUpWithEmailBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel
        binding.backListener = backListener
        binding.signUpListener = signUpListener

        return binding.root
    }

    interface OnSignUpButtonClickListener {
        fun onSignUpButtonClick(email: String, password: String)
    }

    interface OnBackButtonClickListener {
        fun onBackButtonClick()
    }
}
