package com.technion.vedibarta.teacher

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.technion.vedibarta.R
import com.technion.vedibarta.login.ChooseCharacteristicsFragment
import com.technion.vedibarta.login.UserSetupActivity
import com.technion.vedibarta.utilities.CustomViewPager
import com.technion.vedibarta.utilities.SectionsPageAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_user_setup.*

class TeacherSetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_setup)

        setupViewPager(userSetupContainer)
        editTabs.setupWithViewPager(userSetupContainer)
        editTabs.touchables.forEach { it.isEnabled = true }

        VedibartaActivity.changeStatusBarColor(this, R.color.colorBoarding)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupViewPager(viewPager: CustomViewPager) {
        val adapter = SectionsPageAdapter(supportFragmentManager)
        val characteristicsFragment = ChooseCharacteristicsFragment()
//        characteristicsNext = characteristicsFragment as? UserSetupActivity.OnNextClickForCharacteristics
//            ?: throw ClassCastException("$characteristicsFragment must implement ${OnNextClickForCharacteristics::class}")
        adapter.addFragment(TeacherPersonalInfoFragment(), "1")
        //adapter.addFragment(characteristicsFragment, "2")
        //adapter.addFragment(HobbiesFragment(), "3")
        viewPager.setPagingEnabled(false)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1
    }
}
