package com.technion.vedibarta.teacher

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.technion.vedibarta.POJOs.Gender

import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.BubblesSelectionAdapter
import com.technion.vedibarta.data.viewModels.BubbleViewModel
import com.technion.vedibarta.data.viewModels.UserSetupViewModel
import com.technion.vedibarta.databinding.FragmentTeacherProfileBinding
import com.technion.vedibarta.userProfile.ProfileEditActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.fragment_teacher_profile.*
import kotlinx.android.synthetic.main.fragment_teacher_search_extra_options.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

val dummyCharacteristicsList = listOf(
    "ממלכתי",
    "דמוקרטי"
)

val dummySubjectsList = listOf(
    "אנגלית",
    "צרפתית"
)

/**
 * A simple [Fragment] subclass.
 * Use the [TeacherProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeacherProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentTeacherProfileBinding.inflate(inflater, container, false)
        populateWithDummyData(binding)
        binding.actionLogOut.setOnClickListener {
            onLogoutClick()
        }
        binding.actionEditProfile.setOnClickListener {
            startActivity(Intent(context, TeacherEditProfileActivity::class.java))
        }

        return binding.root
    }

    private fun onLogoutClick() {
        val title = TextView(requireContext())
        title.setText(R.string.dialog_logout_title)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary))
        title.gravity = Gravity.CENTER
        title.setPadding(10, 40, 10, 24)

        var msg = R.string.dialog_logout_message_m
        //if (VedibartaActivity.student!!.gender == Gender.FEMALE)
            msg = R.string.dialog_logout_message_f

        val builder = AlertDialog.Builder(requireContext())
        builder.setCustomTitle(title)
            .setMessage(msg)
            .setPositiveButton(R.string.yes) { _, _ ->
                //TODO: Add logout logic
                //performLogout()
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
        builder.create()
    }

    private fun populateRecyclerView(recyclerView: RecyclerView, dataList: List<String>) {val bubblesViewModels = dataList.map {
        val marked = MutableLiveData(true)

        BubbleViewModel(
            MutableLiveData(it),
            marked
        )
    }
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = BubblesSelectionAdapter(viewLifecycleOwner, bubblesViewModels, false)

    }

    private fun populateWithDummyData(binding: FragmentTeacherProfileBinding) {
        binding.profilePicture.setImageDrawable(getDrawable(requireContext(), R.drawable.ic_photo_default_profile_girl))
        binding.profilePicture.visibility = View.VISIBLE
        binding.profilePicturePB.visibility = View.INVISIBLE
        binding.userName.text = "אורית לוי"
        binding.userDescription.text = "תיכון חדרה\nדמוקרטי חדרה"
        populateRecyclerView(binding.characteristicsRecyclerView, dummyCharacteristicsList)
        populateRecyclerView(binding.subjectsRecyclerView, dummySubjectsList)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TeacherProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TeacherProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
