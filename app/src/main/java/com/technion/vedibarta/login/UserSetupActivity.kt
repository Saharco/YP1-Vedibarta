package com.technion.vedibarta.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.Gender
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_user_setup.*
import java.lang.Exception
import java.sql.Timestamp

class UserSetupActivity : VedibartaActivity() {

    private val TAG = "UserSetupActivity"
    private lateinit var sectionsPageAdapter: SectionsPageAdapter

    var chosenCharacteristics = mutableSetOf<String>()
    var chosenHobbies = mutableSetOf<String>()
    var chosenFirstName = ""
    var chosenLastName = ""
    var chosenSchool = ""
    var chosenRegion = ""


    var chosenGender: Gender = Gender.MALE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setup)
        sectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        setupViewPager(userSetupContainer)
        editTabs.setupWithViewPager(userSetupContainer)
        setToolbar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        editTabs.touchables.forEach { it.isEnabled = false }
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupViewPager(viewPager: CustomViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        viewPager.setPagingEnabled(false)
        adapter.addFragment(ChooseGenderFragment(), "1")
        adapter.addFragment(ChooseExtraOptionsFragment(), "2")
        adapter.addFragment(ChooseCharacteristicsFragment(), "3")
        adapter.addFragment(ChooseHobbiesFragment(), "4")
        viewPager.adapter = adapter
    }

    override fun onBackPressed() {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_setup_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //TODO: Add checks that user have chosen all required items and filled all fields
            R.id.actionDoneSetup -> {
                database.saveStudentProfile("$chosenFirstName $chosenLastName", null, chosenRegion,
                    chosenSchool, chosenGender, Timestamp(System.currentTimeMillis()), chosenCharacteristics.toList(), chosenHobbies.toList())
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

        }
        return super.onOptionsItemSelected(item)
    }
}
