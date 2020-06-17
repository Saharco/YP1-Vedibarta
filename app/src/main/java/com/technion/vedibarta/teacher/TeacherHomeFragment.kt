package com.technion.vedibarta.teacher

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.technion.vedibarta.R
import com.technion.vedibarta.chatSearch.ChatSearchActivity
import com.technion.vedibarta.databinding.FragmentTeacherHomeBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
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
        binding.extendedFloatingActionButton.setOnClickListener {
            startActivity(Intent(context, TeacherSearchActivity::class.java))
        }

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TeacherHomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(title: String) =
            TeacherHomeFragment().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                }
            }
    }
}
