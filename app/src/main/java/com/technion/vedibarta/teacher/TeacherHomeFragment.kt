package com.technion.vedibarta.teacher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.technion.vedibarta.R
import com.technion.vedibarta.databinding.FragmentTeacherHomeBinding

private const val TITLE = "ודיברת"

/**
 * A simple [Fragment] subclass.
 * Use the [TeacherHomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeacherHomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentTeacherHomeBinding.inflate(inflater, container, false)
        binding.toolbarTitle.text = title
        binding.extendedFloatingActionButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_global_teacherSearchMatchFragment)
        )

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(title: String) =
            TeacherHomeFragment().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                }
            }
    }
}
