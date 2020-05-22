package com.technion.vedibarta.userProfile

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.FragmentListStateAdapter
import com.technion.vedibarta.data.viewModels.CharacteristicsViewModel
import com.technion.vedibarta.data.viewModels.HobbiesViewModel
import com.technion.vedibarta.data.viewModels.characteristicsViewModelFactory
import com.technion.vedibarta.data.viewModels.hobbiesViewModelFactory
import com.technion.vedibarta.fragments.CharacteristicsFragment
import com.technion.vedibarta.fragments.HobbiesFragment
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import kotlinx.android.synthetic.main.activity_profile_edit.*

class ProfileEditActivity : VedibartaActivity(){

    private val TAG = "ProfileEditActivity"

    private val characteristicsViewModel: CharacteristicsViewModel by viewModels {
        characteristicsViewModelFactory(applicationContext, student!!.gender)
    }

    private val hobbiesViewModel: HobbiesViewModel by viewModels {
        hobbiesViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        Log.d(TAG, "created ProfileEditActivity")

        if (characteristicsViewModel.chosenCharacteristics.isEmpty())
            characteristicsViewModel.chosenCharacteristics.putAll(student!!.characteristics)
        if (hobbiesViewModel.chosenHobbies.isEmpty())
            hobbiesViewModel.chosenHobbies.addAll(student!!.hobbies)
        characteristicsViewModel.startLoading()
        hobbiesViewModel.startLoading()
        combineResources(
            hobbiesViewModel.hobbiesResources,
            characteristicsViewModel.characteristicsResources
        )
            .observe(this, Observer {
                when (it) {
                    is Loaded -> {
                        loading.visibility = View.GONE
                        toolBarLayout.visibility = View.VISIBLE
                        editProfileContainer.visibility = View.VISIBLE
                    }
                    is Error -> Toast.makeText(this, it.reason, Toast.LENGTH_LONG).show()
                    is SlowLoadingEvent -> {
                        if (!it.handled)
                            Toast.makeText(
                                this,
                                resources.getString(R.string.net_error),
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                }
            })
        setupViewPager(editProfileContainer)
        setToolbar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        viewPager.isUserInputEnabled = true
        viewPager.adapter = FragmentListStateAdapter(this, mutableListOf(CharacteristicsFragment(
            student!!.gender), HobbiesFragment()))
        val titleList = listOf(getString(R.string.characteristics_tab_title), getString(R.string.hobbies_tab_title))
        TabLayoutMediator(editTabs, editProfileContainer) { tab, position ->
            tab.text = titleList[position]
        }.attach()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.actionEditProfile -> commitEditChanges()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun commitEditChanges() {
        val startingCharacteristics =  student!!.characteristics
        val startingHobbies = student!!.hobbies
        student!!.characteristics = characteristicsViewModel.chosenCharacteristics
        student!!.hobbies = hobbiesViewModel.chosenHobbies
        database.students().userId().build().set(student!!).addOnSuccessListener {
            Log.d("profileEdit", "saved profile changes")
            setResult(Activity.RESULT_OK)
            finish()
        }.addOnFailureListener {
            Log.d("profileEdit", "${it.message}, cause: ${it.cause?.message}")
            student!!.characteristics = startingCharacteristics
            student!!.hobbies = startingHobbies
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_edit_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        if (changesOccurred()) {
            Log.d(TAG, "Found changes")
            val title = TextView(this)
            title.setText(R.string.edit_discard_changes_title)
            title.textSize = 20f
            title.setTypeface(null, Typeface.BOLD)
            title.setTextColor(ContextCompat.getColor(this, R.color.textPrimary))
            title.gravity = Gravity.CENTER
            title.setPadding(10, 40, 10, 24)
            val builder = AlertDialog.Builder(this)
            builder.setCustomTitle(title)
                .setMessage(R.string.edit_discard_changes_message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    student!!.hobbies = hobbiesViewModel.chosenHobbies
                    student!!.characteristics = characteristicsViewModel.chosenCharacteristics
                    super.onBackPressed()
                }
                .setNegativeButton(R.string.no) { _, _ -> super.onBackPressed()}
                .show()
            builder.create()
            return
        }
        Log.d(TAG, "No changes occurred")
        super.onBackPressed()
    }

    private fun changesOccurred(): Boolean {
        if (student!!.characteristics.keys != characteristicsViewModel.chosenCharacteristics.keys)
            return true
        if (student!!.hobbies.toSet() != hobbiesViewModel.chosenHobbies.toSet())
            return true
        return false
    }

    private fun combineResources(
        hobbiesResourcesLiveData: LiveData<LoadableData<HobbiesViewModel.HobbiesResources>>,
        characteristicsResourcesLiveData: LiveData<LoadableData<CharacteristicsViewModel.CharacteristicsResources>>
    ): LiveData<LoadableData<ProfileEditResources>> {
        val mediator = MediatorLiveData<LoadableData<ProfileEditResources>>()
            .apply { value = NormalLoading() }

        fun refreshCombination() {
            // cannot go back after reaching an end-state
            if (mediator.value is Error) return

            val hobbiesResources = hobbiesResourcesLiveData.value
            val characteristicsResources = characteristicsResourcesLiveData.value

            // become Loaded when all resources have been loaded
            if (hobbiesResources is Loaded
                && characteristicsResources is Loaded
            ) {
                mediator.value = Loaded(
                    ProfileEditResources(
                        allHobbies = hobbiesResources.data.allHobbies,
                        hobbyCardList = hobbiesResources.data.hobbyCardList,
                        allCharacteristics = characteristicsResources.data.allCharacteristics,
                        characteristicsCardList = characteristicsResources.data.characteristicsCardList
                    )
                )
                return
            }

            // become Error when the first error occurs
            hobbiesResources?.let {
                if (it is Error) {
                    mediator.value = Error(it.reason); return
                }
            }
            characteristicsResources?.let {
                if (it is Error) {
                    mediator.value = Error(it.reason); return
                }
            }

            // trigger SlowLoadingEvent when the first SlowLoadingEvent occurs
            if (mediator.value !is SlowLoadingEvent
                && (hobbiesResources is SlowLoadingEvent
                        || characteristicsResources is SlowLoadingEvent)
            ) {
                mediator.value = SlowLoadingEvent()
                return
            }
        }

        mediator.addSource(hobbiesResourcesLiveData) { refreshCombination() }
        mediator.addSource(characteristicsResourcesLiveData) { refreshCombination() }

        return mediator
    }

    data class ProfileEditResources(
        val allCharacteristics: MultilingualTextResource,
        val characteristicsCardList: List<CategoryCard>,
        val allHobbies: MultilingualTextResource,
        val hobbyCardList: List<CategoryCard>
    )

}