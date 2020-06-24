package com.technion.vedibarta.teacher

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.FragmentListStateAdapter
import com.technion.vedibarta.data.viewModels.TeacherSetupViewModel
import com.technion.vedibarta.data.viewModels.TeacherSetupViewModel.*
import com.technion.vedibarta.fragments.TeacherPersonalInfoFragment
import com.technion.vedibarta.fragments.TeacherSetupCharacteristicsSelectionFragment
import com.technion.vedibarta.fragments.TeacherSetupSubjectsSelectionFragment
import com.technion.vedibarta.utilities.extensions.exhaustive
import com.technion.vedibarta.utilities.missingDetailsDialog
import kotlinx.android.synthetic.main.activity_teacher_setup.*
import kotlinx.android.synthetic.main.activity_teacher_setup.backButton
import kotlinx.android.synthetic.main.activity_teacher_setup.doneButton
import kotlinx.android.synthetic.main.activity_teacher_setup.editTabs
import kotlinx.android.synthetic.main.activity_teacher_setup.nextButton
import kotlinx.android.synthetic.main.activity_teacher_setup.userSetupContainer

class TeacherSetupActivity : AppCompatActivity() {

    private val viewModel: TeacherSetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_setup)

        setupViewPager()

        viewModel.doneButtonVisibility.observe(this) {
            doneButton.visibility = it
        }

        viewModel.nextButtonState.observe(this) {
            when (it) {
                is NextButtonState.Gone -> nextButton.visibility = View.GONE
                is NextButtonState.Visible -> {
                    nextButton.visibility = View.VISIBLE
                }
            }
        }

        viewModel.currentScreenIdx.observe(this) {
            userSetupContainer.currentItem = it
        }

        viewModel.event.observe(this) {
            if (it.handled)
                return@observe

            when (it) {
                is Event.Finish -> {
                    startActivity(Intent(this, TeacherMainActivity::class.java))
                    finish()
                }
                is Event.DisplayError -> Toast.makeText(this, it.msgResId, Toast.LENGTH_LONG)
                    .show()
                is Event.DisplayMissingInfoDialog -> missingDetailsDialog(this, getString(it.msgResId))
                is Event.Back -> super.onBackPressed()
            }.exhaustive

            it.handled = true
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_clear_white)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initButtons()
    }

    private fun initButtons() {
        doneButton.bringToFront()
        doneButton.setOnClickListener { viewModel.donePressed() }

        nextButton.setOnClickListener { viewModel.nextPressed() }

        backButton.bringToFront()
        backButton.setOnClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        viewModel.backPressed()
    }

    private fun setupViewPager() {
        val adapter = FragmentListStateAdapter(
            this,
            mutableListOf({
                TeacherPersonalInfoFragment()
            }, {
                TeacherSetupCharacteristicsSelectionFragment()
            }, {
                TeacherSetupSubjectsSelectionFragment()
            }, {
                TeacherScheduleFragment()
            }))

        userSetupContainer.adapter = adapter
        userSetupContainer.isUserInputEnabled = false

        TabLayoutMediator(editTabs, userSetupContainer) { tab, position ->
            tab.text = "${(position + 1)}"
        }.attach()

        editTabs.touchables.forEach { it.isEnabled = false }
    }
}
