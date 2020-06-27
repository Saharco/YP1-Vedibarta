package com.technion.vedibarta.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.POJOs.Teacher
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.FragmentListStateAdapter
import com.technion.vedibarta.data.TeacherMeta
import com.technion.vedibarta.data.viewModels.TeacherSearchMatchViewModel
import com.technion.vedibarta.data.viewModels.TeacherSearchMatchViewModel.*
import com.technion.vedibarta.matching.TeachersMatcher
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.fragment_chat_search.*

class TeacherSearchMatchFragment : Fragment() {

    companion object {
        private const val MINIMUM_TRANSITION_TIME = 900L
    }

    private val viewModel: TeacherSearchMatchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.teacher_search_match_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        setToolbar(toolbar)

        searchButton.setOnClickListener {
            viewModel.searchPressed()
        }

        setupViewPager()

        viewModel.event.observe(viewLifecycleOwner) {
            if (it.handled)
                return@observe

            when (it) {
                is Event.Search -> searchMatch()
                is Event.DisplayFailure -> displayErrorMessage(getString(it.msgResId))
            }

            it.handled = true
        }

        viewFlipper.setInAnimation(requireContext(), android.R.anim.fade_in)
        viewFlipper.setOutAnimation(requireContext(), android.R.anim.fade_out)
    }

    private fun searchMatch() {
        val grades = viewModel.grade

        TeachersMatcher().match(
            viewModel.characteristics,
            grades?.let { Teacher.Grades(grades) },
            viewModel.subjects,
            if (viewModel.searchBySchedule) TeacherMeta.teacher.getSchedule() else null,
            viewModel.region
        ).addOnSuccessListener(requireActivity()) { teachers ->
            val filteredTeachers = teachers.filter { it.uid != TeacherMeta.teacher.uid }
            if (filteredTeachers.isNotEmpty()) {
                val action =
                    TeacherSearchMatchFragmentDirections.actionTeacherSearchMatchFragmentToTeacherCandidatesActivity(
                        filteredTeachers.toTypedArray()
                    )
                Handler().postDelayed({
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        findNavController().navigate(action)
                        viewFlipper.showPrevious()
                    }
                }, MINIMUM_TRANSITION_TIME)
            } else {
                Toast.makeText(requireContext(), R.string.no_matching_teachers, Toast.LENGTH_LONG).show()
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    VedibartaActivity.hideSplash(requireActivity())
                    viewFlipper.showPrevious()
                }
            }
        }.addOnFailureListener(requireActivity()) {
            viewFlipper.showPrevious()
            Toast.makeText(requireContext(), R.string.something_went_wrong, Toast.LENGTH_LONG).show()
        }

        viewFlipper.showNext()
        VedibartaActivity.showSplash(
            requireActivity(),
            getString(R.string.teachers_search_loading_message)
        )
    }

    override fun onStop() {
        super.onStop()
        if (!VedibartaActivity.splashScreen.isIdleNow)
            VedibartaActivity.splashScreen.decrement()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> requireActivity().onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViewPager() {
        val fragments = listOf(
            { TeacherSearchMatchSchoolCharacteristicsSelectionFragment() },
            { TeacherSearchMatchSubjectsSelectionFragment() },
            { TeacherSearchMatchExtraOptionsFragment() }
        )
        searchUserContainer.adapter = FragmentListStateAdapter(this, fragments)
        TabLayoutMediator(editTabs, searchUserContainer) { tab, position ->
            tab.text = "${position + 1}"
        }.attach()
    }

    private fun setToolbar(tb: Toolbar) {
        (activity as AppCompatActivity).setSupportActionBar(tb)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun displayErrorMessage(message: String) {
        val title = TextView(requireContext())
        title.setText(R.string.chat_search_wrong_details_title)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary))
        title.gravity = Gravity.CENTER
        title.setPadding(10, 40, 10, 24)
        val builder = AlertDialog.Builder(requireContext())
        builder.setCustomTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.yes) { _, _ -> }
            .show()
        builder.create()
    }
}