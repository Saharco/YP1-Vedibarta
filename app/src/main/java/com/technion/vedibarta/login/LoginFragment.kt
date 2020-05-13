package com.technion.vedibarta.login


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.SignInViewModel
import com.technion.vedibarta.databinding.FragmentSignInWithEmailBinding
import java.lang.ClassCastException

/**
 * The sign-in with email screen.
 *
 * This class handles the actions taken when any button is pressed on the sign-in with email screen.
 *
 * @see R.layout.fragment_sign_in_with_email
 */
class LoginFragment : Fragment() {
    private val viewModel: SignInViewModel by viewModels()

    private lateinit var backListener: OnBackButtonClickListener
    private lateinit var loginListener: OnLoginButtonClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)

        backListener = context as? OnBackButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnBackButtonClickListener::class}")
        loginListener = context as? OnLoginButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnLoginButtonClickListener::class}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSignInWithEmailBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel
        binding.backListener = backListener
        binding.loginListener = loginListener

        return binding.root
    }

    interface OnLoginButtonClickListener {
        fun onLoginButtonClick(email: String, password: String)
    }

    interface OnBackButtonClickListener {
        fun onBackButtonClick()
    }
}
