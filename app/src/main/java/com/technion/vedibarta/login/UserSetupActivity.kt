package com.technion.vedibarta.login

import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.FragmentListStateAdapter
import com.technion.vedibarta.data.StudentResources
import com.technion.vedibarta.data.viewModels.UserSetupViewModel
import com.technion.vedibarta.data.viewModels.UserSetupViewModel.*
import com.technion.vedibarta.fragments.UserSetupCharacteristicsSelectionFragment
import com.technion.vedibarta.fragments.UserSetupHobbiesSelectionFragment
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_user_setup.*

class UserSetupActivity : VedibartaActivity() {
    companion object {
        private const val TAG = "UserSetupActivity"
    }

    private val viewModel: UserSetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setup)

        setupViewPager()

        viewModel.doneButtonVisibility.observe(this) {
            doneButton.visibility = it
        }

        viewModel.backButtonVisibility.observe(this) {
            backButton.visibility = it
        }

        viewModel.nextButtonState.observe(this) {
            when (it) {
                is NextButtonState.Gone -> nextButton.visibility = View.GONE
                is NextButtonState.Visible -> {
                    nextButton.visibility = View.VISIBLE
                    nextButton.text = getString(it.textResId)
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
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    finish()
                }
                is Event.DisplayError -> Toast.makeText(this, it.msgResId, Toast.LENGTH_LONG)
                    .show()
                is Event.DisplayMissingInfoDialog -> missingDetailsDialog(getString(it.msgResId))
            }

            it.handled = true
        }

        initButtons()
    }

    private fun initButtons() {
        doneButton.bringToFront()
        doneButton.setOnClickListener { viewModel.donePressed(userId!!) }
        nextButton.setOnClickListener { viewModel.nextPressed() }
        backButton.setOnClickListener { onBackPressed() }
        backButton.bringToFront()
    }

    private fun setupViewPager() {
        val characteristicsFragmentList =
            listOf<() -> Fragment> {
                ChoosePersonalInfoFragment()
            } + StudentResources.characteristicsCardList.mapIndexed { idx, _ -> {
                UserSetupCharacteristicsSelectionFragment.newInstance(idx)
            }} + {
                UserSetupHobbiesSelectionFragment()
            }

        userSetupContainer.adapter = FragmentListStateAdapter(this, characteristicsFragmentList)
        userSetupContainer.isUserInputEnabled = false

        TabLayoutMediator(editTabs, userSetupContainer) { tab, position ->
            tab.text = "${(position + 1)}"
        }.attach()

        editTabs.touchables.forEach { it.isEnabled = false }
    }

    override fun onBackPressed() {
        viewModel.backPressed()
    }

    private fun missingDetailsDialog(msg: String) {
        val message = SpannableStringBuilder()
            .bold {
                append(msg).setSpan(
                    RelativeSizeSpan(1f),
                    0,
                    msg.length,
                    0
                )
            }

        val text = resources.getString(R.string.user_setup_missing_details_dialog_title)

        val titleText = SpannableStringBuilder()
            .bold { append(text).setSpan(RelativeSizeSpan(1.2f), 0, text.length, 0) }
        titleText.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length, 0)

        val positiveButtonText = SpannableStringBuilder()
            .bold { append(resources.getString(R.string.ok)) }

        val builder = AlertDialog.Builder(this)
        builder
            .setTitle(titleText)
            .setIcon(ContextCompat.getDrawable(this, R.drawable.ic_error))
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { _, _ -> }

        builder.create().show()
    }

    fun onRadioButtonClicked(view: View) {
        when (view.id) {
            R.id.gradeTenth -> viewModel.grade = Grade.TENTH
            R.id.gradeEleventh -> viewModel.grade = Grade.ELEVENTH
            R.id.gradeTwelfth -> viewModel.grade = Grade.TWELFTH
        }
    }
}
