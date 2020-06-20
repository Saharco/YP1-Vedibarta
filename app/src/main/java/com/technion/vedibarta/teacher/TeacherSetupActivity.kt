package com.technion.vedibarta.teacher

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.POJOs.Error
import com.technion.vedibarta.POJOs.LoadableData
import com.technion.vedibarta.POJOs.Loaded
import com.technion.vedibarta.POJOs.SlowLoadingEvent
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.FragmentListStateAdapter
import com.technion.vedibarta.data.viewModels.SchoolInfo
import com.technion.vedibarta.data.viewModels.TeacherSetupResources
import com.technion.vedibarta.data.viewModels.TeacherSetupViewModel
import com.technion.vedibarta.data.viewModels.teacherSetupViewModelFactory
import com.technion.vedibarta.fragments.TeacherPersonalInfoFragment
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_teacher_setup.*
import kotlinx.android.synthetic.main.activity_teacher_setup.toolbar
import kotlinx.android.synthetic.main.activity_teacher_setup.toolbarLayout

const val SCHOOL_CHARACTERISTICS = "school characteristics"
const val TEACHER_SUBJECTS = "teacher subjects"

class TeacherSetupActivity : AppCompatActivity(), TeacherPersonalInfoFragment.SchoolPressHandlers {

    private val viewModel: TeacherSetupViewModel by viewModels {
        teacherSetupViewModelFactory(applicationContext)
    }

    private var loadedHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_setup)
        viewModel.teacherSetupResources.observe(this, Observer {
            observerHandler(viewModel.teacherSetupResources)
        })
        viewModel.event.observe(this){event ->
            if (!event.handled) {
                when (event) {
                    is TeacherSetupViewModel.Event.ToggleActionBar -> toggleCustomToolbar()
                    is TeacherSetupViewModel.Event.UpdateTitle -> updateSelectedTitle()
                    is TeacherSetupViewModel.Event.DisplayError -> Toast.makeText(
                        this,
                        event.msgResId,
                        Toast.LENGTH_LONG
                    ).show()
                    else -> return@observe
                }
                event.handled = true
            }
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_clear_white)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.item_actions_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.delete -> {
                viewModel.removeSelectedSchool()
            }
            R.id.edit -> viewModel.openSchoolDialog(true)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!viewModel.handleOnBackPress())
            super.onBackPressed()
    }

    private fun toggleCustomToolbar() {
        if (viewModel.itemActionBarEnabled) {
            toolbarLayout.visibility = View.VISIBLE
            toolbar.menu.clear()
            menuInflater.inflate(R.menu.item_actions_menu, toolbar.menu)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_clear_white)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.black
                )
            )
            VedibartaActivity.changeStatusBarColor(
                this,
                ContextCompat.getColor(this, android.R.color.black)
            )
        } else {
            toolbarLayout.visibility = View.GONE
            toolbar.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimary
                )
            )
        }
    }

    private fun updateSelectedTitle() {
        if (viewModel.selectedItems == 1) {
            toolbar.menu.clear()
            menuInflater.inflate(R.menu.item_actions_menu, toolbar.menu)
            supportActionBar?.title =
                "${viewModel.selectedItems} ${getString(R.string.single_item_selected)}"
        } else {
            toolbar.menu.removeItem(R.id.edit)
            supportActionBar?.title =
                "${viewModel.selectedItems} ${getString(R.string.multi_item_selected)}"
        }
    }

    private fun observerHandler(liveData: LiveData<LoadableData<TeacherSetupResources>>) {
        when (val value = liveData.value) {
            is Loaded -> {
                if (!loadedHandled) {
                    loading.visibility = View.GONE
                    setupViewPager(userSetupContainer, value.data)
                    loadedHandled = true
                }
            }
            is Error -> Toast.makeText(this, value.reason, Toast.LENGTH_LONG).show()
            is SlowLoadingEvent -> {
                if (!value.handled)
                    Toast.makeText(
                        this,
                        resources.getString(R.string.net_error),
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }
    }

    private fun setupViewPager(
        viewPager: ViewPager2,
        data: TeacherSetupResources
    ) {
        val adapter = FragmentListStateAdapter(
            this,
            mutableListOf({
                TeacherPersonalInfoFragment.newInstance(
                    data.schoolsName.getAll().toTypedArray(),
                    data.regionsName.getAll().toTypedArray()
                ) }, {
                TeacherCharacteristicsFragment.newInstance(SCHOOL_CHARACTERISTICS)
            }, {
                TeacherCharacteristicsFragment.newInstance(TEACHER_SUBJECTS)
            }, {
                TeacherScheduleFragment()
            }))
        nextButton.setOnClickListener {
            viewPager.currentItem += 1
        }
        viewPager.isUserInputEnabled = false
        viewPager.adapter = adapter
        TabLayoutMediator(editTabs, userSetupContainer) { tab, position ->
            tab.text = "${(position + 1)}"
        }.attach()
        editTabs.touchables.forEach { it.isEnabled = false }
    }

    override fun onLongPress(v: View): Boolean {
        viewModel.beginSchoolExtraActions(v as MaterialCardView)
        return true
    }

    override fun onClick(v: View): Boolean {
        if (viewModel.itemActionBarEnabled) {
            if ((v as MaterialCardView).isChecked) {
                viewModel.unSelectSchool(v)
            } else {
                viewModel.selectSchool(v)
            }
        }
        return true
    }


}

sealed class TeacherResult
data class Success(val data: SchoolInfo) : TeacherResult()
data class Failure(val msg: String) : TeacherResult()
