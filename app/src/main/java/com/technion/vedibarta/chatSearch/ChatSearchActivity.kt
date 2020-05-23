package com.technion.vedibarta.chatSearch

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import com.technion.vedibarta.chatCandidates.ChatCandidatesActivity
import com.technion.vedibarta.data.viewModels.CharacteristicsViewModel
import com.technion.vedibarta.data.viewModels.ChatSearchViewModel
import com.technion.vedibarta.data.viewModels.characteristicsViewModelFactory
import com.technion.vedibarta.data.viewModels.chatSearchViewModelFactory
import com.technion.vedibarta.fragments.CharacteristicsFragment
import com.technion.vedibarta.matching.StudentsMatcher
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.extensions.isInForeground
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.TextResource
import kotlinx.android.synthetic.main.activity_chat_search.*
import kotlinx.android.synthetic.main.activity_chat_search.editTabs
import kotlinx.android.synthetic.main.activity_chat_search.loading
import kotlinx.android.synthetic.main.activity_chat_search.toolbar

class ChatSearchActivity : VedibartaActivity() {

    companion object {
        private const val MATCHING_TIMEOUT = 10L
        private const val MINIMUM_TRANSITION_TIME = 900L
        private const val TAG = "ChatSearchActivity"
    }

    private val characteristicsViewModel: CharacteristicsViewModel by viewModels {
        characteristicsViewModelFactory(applicationContext, Gender.NONE)
    }

    private val chatSearchViewModel: ChatSearchViewModel by viewModels {
        chatSearchViewModelFactory(applicationContext)
    }
    private lateinit var chatSearchResources: LiveData<LoadableData<ChatSearchResources>>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_search)
        Log.d(TAG, "ChatSearchActivity Created")

        characteristicsViewModel.startLoading()

        chatSearchResources = combineResources(
            chatSearchViewModel.schoolsName,
            chatSearchViewModel.regionsName,
            characteristicsViewModel.characteristicsResources
        )

        chatSearchResources.observe(this, Observer {
            when (it) {
                is Loaded -> {
                    loading.visibility = View.GONE
                    editTabs.visibility = View.VISIBLE
                    toolBarLayout.visibility = View.VISIBLE
                    searchUserContainer.visibility = View.VISIBLE
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

        setupViewPager(searchUserContainer)
        setToolbar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        viewFlipper.setInAnimation(this, android.R.anim.fade_in)
        viewFlipper.setOutAnimation(this, android.R.anim.fade_out)
    }

    override fun onStop() {
        super.onStop()
        if (!splashScreen.isIdleNow)
            splashScreen.decrement()
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_search_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionChatSearch -> {
                //TODO pass the chosen filters to database/other activity fo rmatching
                when(val result = validateChosenDetails()){
                    is Success -> searchMatch()
                    is Failure -> displayErrorMessage(result.msg)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onRadioButtonClicked(view: View) {
        when (view.id) {
            R.id.gradeTenth -> chatSearchViewModel.grade = Grade.TENTH
            R.id.gradeEleventh -> chatSearchViewModel.grade = Grade.ELEVENTH
            R.id.gradeTwelfth -> chatSearchViewModel.grade = Grade.TWELFTH
        }
    }

    private fun displayErrorMessage(message: String) {
        val title = TextView(this)
        title.setText(R.string.chat_search_wrong_details_title)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(ContextCompat.getColor(this, R.color.textPrimary))
        title.gravity = Gravity.CENTER
        title.setPadding(10, 40, 10, 24)
        val builder = AlertDialog.Builder(this)
        builder.setCustomTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.yes) { _, _ -> }
            .show()
        builder.create()
    }

    private fun validateChosenDetails(): SearchResult {
        val resourcesCombo = chatSearchResources.value as? Loaded
            ?: error("tried to validate the user before done loading resources")

        when(val region = chatSearchViewModel.chosenRegion){
            is Filled -> {
                if (!resourcesCombo.data.regionsName.getAll().contains(region.text))
                    return Failure(getString(R.string.chat_search_wrong_region_message))
            }
        }

        when(val school = chatSearchViewModel.chosenSchool){
            is Filled ->{
                if (!resourcesCombo.data.schoolsName.getAll().contains(school.text))
                    return Failure(getString(R.string.chat_search_wrong_school_message))
            }
        }

        if (characteristicsViewModel.chosenCharacteristics.isEmpty())
            return Failure(getString(R.string.chat_search_no_characteristics_chosen_message))

        return Success
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        viewPager.isUserInputEnabled = true
        viewPager.adapter = FragmentListStateAdapter(
            this,
            mutableListOf({CharacteristicsFragment()}, {SearchExtraOptionsFragment()})
        )
        TabLayoutMediator(editTabs, searchUserContainer) { tab, position ->
            tab.text = "${position + 1}"
        }.attach()
    }

    private fun searchMatch() {
        Log.d(TAG, "Searching a match")
        val attributes: SearchAttributes = getSearchAttributes()

        StudentsMatcher().match(
            attributes.characteristics,
            attributes.region,
            attributes.school
        ).addOnSuccessListener(this) { students ->
            val filteredStudents = students.filter { it.uid != student!!.uid }
            if (filteredStudents.isNotEmpty()) {
                Log.d(TAG, "Matched students successfully")

                val intent = Intent(this, ChatCandidatesActivity::class.java)
                intent.putExtra("STUDENTS", filteredStudents.toTypedArray())

                Handler().postDelayed({
                    if (this@ChatSearchActivity.isInForeground()) {
                        startActivity(intent)
                        finish()
                    }
                }, MINIMUM_TRANSITION_TIME)

            } else {
                Log.d(TAG, "No matching students found")
                Toast.makeText(this, R.string.no_matching_students, Toast.LENGTH_LONG).show()
                if (this@ChatSearchActivity.isInForeground()) {
                    hideSplash()
                    viewFlipper.showPrevious()
                }
            }
        }.addOnFailureListener(this) { exp ->
            Log.w(TAG, "Matching students failed", exp)
            if (this@ChatSearchActivity.isInForeground()) {
                if (isInternetAvailable(this@ChatSearchActivity))
                //TODO: retry search!
                else
                    onBackPressed()
            }
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
        }

        viewFlipper.showNext()
        showSplash(getString(R.string.chat_search_loading_message))
    }

    private fun getSearchAttributes(): SearchAttributes {
        val school = when(val school = chatSearchViewModel.chosenSchool){
            is Filled -> school.text
            else -> null
        }
        val region = when(val region = chatSearchViewModel.chosenRegion){
            is Filled -> region.text
            else -> null
        }
        return SearchAttributes(characteristicsViewModel.chosenCharacteristics, region, school, chatSearchViewModel.grade)
    }

    @Suppress("DEPRECATION")
    private fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = true
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = true
                    }
                }
            }
        }
        return result
    }

    private fun combineResources(
        schoolsNameLiveData: LiveData<LoadableData<TextResource>>,
        regionsNameLiveData: LiveData<LoadableData<TextResource>>,
        characteristicsResourcesLiveData: LiveData<LoadableData<CharacteristicsViewModel.CharacteristicsResources>>
    ): LiveData<LoadableData<ChatSearchResources>> {
        val mediator = MediatorLiveData<LoadableData<ChatSearchResources>>()
            .apply { value = NormalLoading() }

        fun refreshCombination() {
            // cannot go back after reaching an end-state
            if (mediator.value is Error) return

            val schoolsName = schoolsNameLiveData.value
            val regionsName = regionsNameLiveData.value
            val characteristicsResources = characteristicsResourcesLiveData.value

            // become Loaded when all resources have been loaded
            if (schoolsName is Loaded
                && regionsName is Loaded
                && characteristicsResources is Loaded
            ) {
                mediator.value = Loaded(
                    ChatSearchResources(
                        schoolsName = schoolsName.data,
                        regionsName = regionsName.data,
                        allCharacteristics = characteristicsResources.data.allCharacteristics,
                        characteristicsCardList = characteristicsResources.data.characteristicsCardList
                    )
                )
                return
            }

            // become Error when the first error occurs
            schoolsName?.let {
                if (it is Error) {
                    mediator.value = Error(it.reason); return
                }
            }
            regionsName?.let {
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
                && (schoolsName is SlowLoadingEvent
                        || regionsName is SlowLoadingEvent
                        || characteristicsResources is SlowLoadingEvent)
            ) {
                mediator.value = SlowLoadingEvent()
                return
            }
        }

        mediator.addSource(schoolsNameLiveData) { refreshCombination() }
        mediator.addSource(regionsNameLiveData) { refreshCombination() }
        mediator.addSource(characteristicsResourcesLiveData) { refreshCombination() }

        return mediator
    }


    data class ChatSearchResources(
        val allCharacteristics: MultilingualTextResource,
        val characteristicsCardList: List<CategoryCard>,
        val schoolsName: TextResource,
        val regionsName: TextResource
        )

    data class SearchAttributes(
        val characteristics: Collection<String>,
        val region: String?,
        val school: String?,
        val grade: Grade
    )
}
sealed class SearchResult

object Success : SearchResult()

data class Failure(val msg: String) : SearchResult()