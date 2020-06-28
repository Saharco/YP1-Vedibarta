package com.technion.vedibarta.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.FragmentListStateAdapter
import com.technion.vedibarta.data.viewModels.TeacherProfileEditViewModel
import com.technion.vedibarta.data.viewModels.TeacherProfileEditViewModel.*
import com.technion.vedibarta.utilities.extensions.exhaustive
import kotlinx.android.synthetic.main.teacher_edit_profile_fragment.*

class TeacherProfileEditFragment : Fragment() {

    val viewModel: TeacherProfileEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.backPressed()
        }
        return inflater.inflate(R.layout.teacher_edit_profile_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkButton.setOnClickListener { viewModel.commitChangesPressed() }

        setupViewPager()
        setToolbar()

        viewModel.event.observe(viewLifecycleOwner) {
            if (it.handled)
                return@observe

            when (it) {
                is Event.DisplayConfirmationDialog -> displayConfirmationDialog()
                is Event.DisplayError -> Toast.makeText(requireContext(), it.errorMsgId, Toast.LENGTH_LONG).show()
                is Event.Finish.Cancel -> findNavController().navigateUp()
                is Event.Finish.Success -> {
                    Toast.makeText(requireContext(), R.string.edit_changes_saved_successfully, Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
            }.exhaustive

            it.handled = true
        }
    }

    private fun setupViewPager() {
        editProfileContainer.isUserInputEnabled = true

        val fragments = listOf(
            { TeacherProfileEditCharacteristicsSelectionFragment() },
            { TeacherProdileEditSubjectsSelectionFragment() },
            { TeacherProfileEditScheduleFragment() }
        )

        editProfileContainer.adapter = FragmentListStateAdapter(this, fragments)
        val titleList = listOf(getString(R.string.schools_characteristics_title), getString(R.string.teaching_subjects_title), getString(R.string.schedule))
        TabLayoutMediator(editTabs, editProfileContainer) { tab, position ->
            tab.text = titleList[position]
        }.attach()
    }

    private fun setToolbar() {
        val appCompat = (activity as AppCompatActivity)
        appCompat.supportActionBar?.setDisplayShowTitleEnabled(false)
        appCompat.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        appCompat.supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun displayConfirmationDialog() {
        val title = TextView(requireContext()).apply {
            setText(R.string.edit_discard_changes_title)
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary))
            gravity = Gravity.CENTER
            setPadding(10, 40, 10, 24)
        }
        val builder = AlertDialog.Builder(requireContext())
        builder.setCustomTitle(title)
            .setMessage(R.string.edit_discard_changes_message)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                viewModel.confirmationCancelPressed()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        builder.create()
    }
}