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
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.FragmentListStateAdapter
import com.technion.vedibarta.chatCandidates.ChatCandidatesActivity
import com.technion.vedibarta.data.viewModels.ChatSearchViewModel
import com.technion.vedibarta.data.viewModels.ChatSearchViewModel.*
import com.technion.vedibarta.fragments.CategorizedBubblesSelectionFragment
import com.technion.vedibarta.matching.StudentsMatcher
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.extensions.isInForeground
import kotlinx.android.synthetic.main.activity_chat_search.*
import kotlinx.android.synthetic.main.activity_chat_search.editTabs
import kotlinx.android.synthetic.main.activity_chat_search.loading
import kotlinx.android.synthetic.main.activity_chat_search.toolbar

class ChatSearchActivity :
    VedibartaActivity(),
    CategorizedBubblesSelectionFragment.ArgumentsSupplier
{
    companion object {
        private const val MINIMUM_TRANSITION_TIME = 900L
        private const val TAG = "ChatSearchActivity"
    }

    private val viewModel: ChatSearchViewModel by viewModels()

    private val chatSearchResources: ChatSearchResources by lazy {
        (viewModel.resources.value as Loaded).data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_search)
        Log.d(TAG, "ChatSearchActivity Created")

        viewModel.resources.observe(this) {
            when (it) {
                is Loaded -> {
                    setupViewPager()
                    setToolbar()

                    loading.visibility = View.GONE
                    editTabs.visibility = View.VISIBLE
                    toolBarLayout.visibility = View.VISIBLE
                    searchUserContainer.visibility = View.VISIBLE
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
                is Event.Search -> searchMatch()
                is Event.DisplayFailure -> displayErrorMessage(getString(it.msgResId))
            }

            it.handled = true
        }

        viewFlipper.setInAnimation(this, android.R.anim.fade_in)
        viewFlipper.setOutAnimation(this, android.R.anim.fade_out)
    }

    override fun onStop() {
        super.onStop()
        if (!splashScreen.isIdleNow)
            splashScreen.decrement()
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupViewPager() {
        searchUserContainer.isUserInputEnabled = true

        val fragments = listOf({
            CategorizedBubblesSelectionFragment.newInstance("characteristics")
        }, {
            SearchExtraOptionsFragment()
        })

        searchUserContainer.adapter = FragmentListStateAdapter(this, fragments)
        TabLayoutMediator(editTabs, searchUserContainer) { tab, position ->
            tab.text = "${position + 1}"
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_search_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionChatSearch -> viewModel.searchPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun onRadioButtonClicked(view: View) {
        when (view.id) {
            R.id.gradeTenth -> viewModel.grade = Grade.TENTH
            R.id.gradeEleventh -> viewModel.grade = Grade.ELEVENTH
            R.id.gradeTwelfth -> viewModel.grade = Grade.TWELFTH
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

    private fun searchMatch() {
        val attributes: SearchAttributes = getSearchAttributes()

        StudentsMatcher().match(
            attributes.characteristics,
            attributes.region,
            attributes.school,
            attributes.grade
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
            hideSplash()
            viewFlipper.showPrevious()
        }

        viewFlipper.showNext()
        showSplash(getString(R.string.chat_search_loading_message))
    }

    private fun getSearchAttributes(): SearchAttributes {
        val school = when (val school = viewModel.chosenSchool) {
            is Filled -> school.text
            else -> null
        }

        val region = when (val region = viewModel.chosenRegion) {
            is Filled -> region.text
            else -> null
        }

        val grade = when (val grade = viewModel.grade) {
            Grade.NONE -> null
            else -> grade
        }

        return SearchAttributes(viewModel.selectedCharacteristics, region, school, grade)
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

    override fun getCategorizedBubblesSelectionArguments(identifier: String): CategorizedBubblesSelectionFragment.Arguments {
        return CategorizedBubblesSelectionFragment.Arguments(
            MutableLiveData(chatSearchResources.characteristicsTranslator),
            chatSearchResources.characteristicsCardList,
            { viewModel.selectedCharacteristics = it }
        )
    }

    data class SearchAttributes(
        val characteristics: Collection<String>,
        val region: String?,
        val school: String?,
        val grade: Grade?
    )
}