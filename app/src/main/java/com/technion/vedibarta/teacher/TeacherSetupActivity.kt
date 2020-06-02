package com.technion.vedibarta.teacher

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.POJOs.Error
import com.technion.vedibarta.POJOs.LoadableData
import com.technion.vedibarta.POJOs.Loaded
import com.technion.vedibarta.POJOs.SlowLoadingEvent
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.FragmentListStateAdapter
import com.technion.vedibarta.data.viewModels.TeacherSetupResources
import com.technion.vedibarta.data.viewModels.TeacherSetupViewModel
import com.technion.vedibarta.data.viewModels.teacherSetupViewModelFactory
import com.technion.vedibarta.fragments.SchoolListItemLongCLick
import com.technion.vedibarta.fragments.TeacherPersonalInfoFragment
import kotlinx.android.synthetic.main.activity_teacher_setup.*

class TeacherSetupActivity : AppCompatActivity(),
    SchoolListItemLongCLick {

    private val teacherSetupViewModel: TeacherSetupViewModel by viewModels {
        teacherSetupViewModelFactory(applicationContext)
    }

    private var loadedHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_setup)
        teacherSetupViewModel.teacherSetupResources.observe(this, Observer {
            observerHandler(teacherSetupViewModel.teacherSetupResources)
        })
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_clear_white)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.school_item_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                toolbarLayout.visibility = View.GONE
                teacherSetupViewModel.selectedItems = 0
                teacherSetupViewModel.selectedItemsList.forEach {
                    it.setOnClickListener {  }
                    it.isLongClickable= true
                    it.isChecked = false
                }
                teacherSetupViewModel.selectedItemsList.clear()
            }
            R.id.edit -> {
            }
            R.id.delete -> {
            }
        }
        return super.onOptionsItemSelected(item)
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
                )
            })
        )
        viewPager.isUserInputEnabled = false
        viewPager.adapter = adapter
        TabLayoutMediator(editTabs, userSetupContainer) { tab, position ->
            tab.text = "${(position + 1)}"
        }.attach()
        editTabs.touchables.forEach { it.isEnabled = false }
    }

    override fun onLongClickListener(v: View): Boolean {
        teacherSetupViewModel.selectedItems++
        teacherSetupViewModel.selectedItemsList.add(v as MaterialCardView)
        supportActionBar?.title = "${teacherSetupViewModel.selectedItems} Selected"
        if (teacherSetupViewModel.selectedItems > 1) {
            toolbar.menu.removeItem(R.id.edit)
        }
        v.isLongClickable = false
        v.isChecked = true
        v.setOnClickListener {
            teacherSetupViewModel.selectedItems--
            teacherSetupViewModel.selectedItemsList.remove(it)
            if (teacherSetupViewModel.selectedItems == 0) {
                toolbarLayout.visibility = View.GONE
            }
            if (teacherSetupViewModel.selectedItems == 1){
                toolbar.menu.clear()
                menuInflater.inflate(R.menu.school_item_menu, toolbar.menu)
            }
            supportActionBar?.title = "${teacherSetupViewModel.selectedItems} Selected"
            v.isChecked = false
            v.isLongClickable = true
            v.setOnClickListener { }
        }
        toolbarLayout.visibility = View.VISIBLE
        return true
    }


}
