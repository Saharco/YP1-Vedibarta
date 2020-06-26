package com.technion.vedibarta.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.FragmentListStateAdapter
import com.technion.vedibarta.data.viewModels.TeacherSearchMatchViewModel
import kotlinx.android.synthetic.main.fragment_chat_search.*

class TeacherSearchMatchFragment : Fragment() {

    private val viewModel: TeacherSearchMatchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.teacher_search_match_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        setToolbar(toolbar)

        setupViewPager()
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
}