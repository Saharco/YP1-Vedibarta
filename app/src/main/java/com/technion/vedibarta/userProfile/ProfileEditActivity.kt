package com.technion.vedibarta.userProfile

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.FragmentListStateAdapter
import com.technion.vedibarta.data.viewModels.ProfileEditViewModel
import com.technion.vedibarta.data.viewModels.ProfileEditViewModel.*
import com.technion.vedibarta.fragments.CategorizedBubblesSelectionFragment
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.extensions.exhaustive
import kotlinx.android.synthetic.main.activity_profile_edit.*

class ProfileEditActivity :
    VedibartaActivity(),
    CategorizedBubblesSelectionFragment.ArgumentsSupplier
{
    companion object {
        private const val TAG = "ProfileEditActivity"
    }

    val viewModel: ProfileEditViewModel by viewModels()

    private val profileEditResources: ProfileEditResources by lazy {
        (viewModel.resources.value as Loaded).data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        viewModel.resources.observe(this) {
            when (it) {
                is Loaded -> {
                    setupViewPager()
                    setToolbar()

                    loading.visibility = View.GONE
                    toolBarLayout.visibility = View.VISIBLE
                    editProfileContainer.visibility = View.VISIBLE
                }
                is Error -> Toast.makeText(this, it.reason, Toast.LENGTH_LONG).show()
                is SlowLoadingEvent -> {
                    if (!it.handled)
                        Toast.makeText(this, resources.getString(R.string.net_error), Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.event.observe(this) {
            if (it.handled)
                return@observe

            when (it) {
                is Event.DisplayConfirmationDialog -> displayConfirmationDialog()
                is Event.DisplayError -> Toast.makeText(this, it.errorMsgId, Toast.LENGTH_LONG).show()
                is Event.Finish.Cancel -> { setResult(Activity.RESULT_CANCELED); finish() }
                is Event.Finish.Success -> { setResult(Activity.RESULT_OK); finish() }
            }.exhaustive

            it.handled = true
        }
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupViewPager() {
        editProfileContainer.isUserInputEnabled = true

        val fragments = listOf({
            CategorizedBubblesSelectionFragment.newInstance("characteristics")
        }, {
            CategorizedBubblesSelectionFragment.newInstance("hobbies")
        })

        editProfileContainer.adapter = FragmentListStateAdapter(this, fragments)
        val titleList = listOf(getString(R.string.characteristics_tab_title), getString(R.string.hobbies_tab_title))
        TabLayoutMediator(editTabs, editProfileContainer) { tab, position ->
            tab.text = titleList[position]
        }.attach()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.actionEditProfile -> viewModel.commitChangesPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_edit_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() = viewModel.backPressed()

    private fun displayConfirmationDialog() {
        val title = TextView(this).apply {
            setText(R.string.edit_discard_changes_title)
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@ProfileEditActivity, R.color.textPrimary))
            gravity = Gravity.CENTER
            setPadding(10, 40, 10, 24)
        }
        val builder = AlertDialog.Builder(this)
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

    override fun getCategorizedBubblesSelectionArguments(identifier: String): CategorizedBubblesSelectionFragment.Arguments =
        when (identifier) {
            "hobbies" -> CategorizedBubblesSelectionFragment.Arguments(
                MutableLiveData(profileEditResources.hobbiesTranslator),
                profileEditResources.hobbyCardList,
                { viewModel.selectedHobbies = it },
                viewModel.selectedHobbies
            )
            "characteristics" -> CategorizedBubblesSelectionFragment.Arguments(
                MutableLiveData(profileEditResources.characteristicsTranslator),
                profileEditResources.characteristicsCardList,
                { viewModel.selectedCharacteristics = it },
                viewModel.selectedCharacteristics
            )
            else -> error("error")
        }
}