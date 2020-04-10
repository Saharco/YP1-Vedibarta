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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.HobbyCard
import com.technion.vedibarta.R
import com.technion.vedibarta.fragments.CharacteristicsFragment
import com.technion.vedibarta.fragments.HobbiesFragment
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.extensions.executeAfterTimeoutInMillis
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import kotlinx.android.synthetic.main.activity_profile_edit.*

class ProfileEditActivity : VedibartaActivity(), VedibartaFragment.ArgumentTransfer {

    private val TAG = "ProfileEditActivity"
    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    lateinit var hobbyCardTask: Task<List<HobbyCard>>
    lateinit var hobbiesResourceTask: Task<MultilingualResource>

    lateinit var characteristicsTask : Task<MultilingualResource>

    private var startingCharacteristics = student!!.characteristics.toMutableMap()
    private var startingHobbies = student!!.hobbies.toMutableSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        Log.d(TAG, "created ProfileEditActivity")

        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        setupViewPager(editProfileContainer)

        hobbiesResourceTask = RemoteResourcesManager(this)
            .findMultilingualResource("hobbies/all")

        hobbyCardTask = VedibartaFragment.loadHobbies(this)

        characteristicsTask = RemoteResourcesManager(this).findMultilingualResource("characteristics")

        Tasks.whenAll(hobbiesResourceTask, hobbyCardTask, characteristicsTask)
            .executeAfterTimeoutInMillis { internetConnectionErrorHandler(this) }
            .addOnSuccessListener(this) {
                loading.visibility = View.GONE
                editTabs.visibility = View.VISIBLE
                editProfileContainer.visibility = View.VISIBLE
            }

        editTabs.setupWithViewPager(editProfileContainer)
        setToolbar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        adapter.addFragment(CharacteristicsFragment(), getString(R.string.characteristics_tab_title))
        adapter.addFragment(HobbiesFragment(), getString(R.string.hobbies_tab_title))
        viewPager.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.actionEditProfile -> commitEditChanges()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun commitEditChanges() {
//        Toast.makeText(this, "Committing changes", Toast.LENGTH_LONG).show()
        //TODO: push to the database first!
         startingCharacteristics = student!!.characteristics
        startingHobbies = student!!.hobbies.toMutableSet()
        database.students().userId().build().set(student!!).addOnSuccessListener {
            Log.d("profileEdit", "saved profile changes")
            setResult(Activity.RESULT_OK)
            finish()
        }.addOnFailureListener {
            Log.d("profileEdit", "${it.message}, cause: ${it.cause?.message}")
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
            title.setTextColor(ContextCompat.getColor(this,R.color.textPrimary))
            title.gravity = Gravity.CENTER
            title.setPadding(10, 40, 10, 24)
            val builder = AlertDialog.Builder(this)
            builder.setCustomTitle(title)
                .setMessage(R.string.edit_discard_changes_message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    student!!.hobbies = startingHobbies.toList()
                    student!!.characteristics = startingCharacteristics
                    super.onBackPressed() }
                .setNegativeButton(R.string.no) { _, _ -> }
                .show()
            builder.create()
            return
        }
        Log.d(TAG, "No changes occurred")
        super.onBackPressed()
    }

    private fun changesOccurred(): Boolean {
        if (startingCharacteristics.filter { it.value } != student!!.characteristics.filter { it.value })
            return true
        if (!(startingHobbies.containsAll(student!!.hobbies.toSet()) &&
                    student!!.hobbies.toSet().containsAll(startingHobbies))
        )
            return true
        return false
    }

    override fun getArgs(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["student"] = student!!
        map["characteristicsTask"] = characteristicsTask
        map["hobbiesResourceTask"] = hobbiesResourceTask
        map["hobbyCardTask"] = hobbyCardTask
        map["activity"] = this
        return map
    }
}