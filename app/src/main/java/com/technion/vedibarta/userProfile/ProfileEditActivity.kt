package com.technion.vedibarta.userProfile

import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_profile_edit.*
import androidx.viewpager.widget.ViewPager

class ProfileEditActivity : VedibartaActivity() {

    private val TAG = "ProfileEditActivity"
    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    public var editedCharacteristics = student!!.characteristics.toMutableSet()
    public var editedHobbies = student!!.hobbies.toMutableSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        Log.d(TAG, "created ProfileEditActivity")

        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        setupViewPager(editProfileContainer)

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
        adapter.addFragment(ProfileEditCharacteristicsFragment(), "מאפייני זהות")
        adapter.addFragment(ProfileEditHobbiesFragment(), "תחביבים")
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
        Toast.makeText(this, "Committing changes", Toast.LENGTH_LONG).show()
        //TODO: push to the database first!
        student!!.characteristics = editedCharacteristics.toList()
        student!!.hobbies = editedHobbies.toList()
        onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_edit_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        if (changesOccurred()) {
            Log.d(TAG, "Found changes")
            val title = TextView(this)
            title.setText(R.string.edit_discard_changes_title)
            title.textSize = 20f
            title.setTypeface(null, Typeface.BOLD)
            title.setTextColor(resources.getColor(R.color.textPrimary))
            title.gravity = Gravity.CENTER
            title.setPadding(10, 40, 10, 24)
            val builder = AlertDialog.Builder(this)
            builder.setCustomTitle(title)
                .setMessage(R.string.edit_discard_changes_message)
                .setPositiveButton(android.R.string.yes) { _, _ -> super.onBackPressed() }
                .setNegativeButton(android.R.string.no) { _, _ -> }
                .show()
            builder.create()
            return
        }
        Log.d(TAG, "No changes occurred")
        super.onBackPressed()
    }

    private fun changesOccurred(): Boolean {
        if (!(editedCharacteristics.containsAll(student!!.characteristics.toSet()) &&
                    student!!.characteristics.toSet().containsAll(editedCharacteristics))
        )
            return true
        if (!(editedHobbies.containsAll(student!!.hobbies.toSet()) &&
                    student!!.hobbies.toSet().containsAll(editedHobbies))
        )
            return true
        return false
    }
}