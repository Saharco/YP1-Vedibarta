package com.technion.vedibarta.utilities

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter


class SectionsPageAdapter(val fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragmentsList = mutableListOf<Fragment>()
    private val fragmentsTitleList = mutableListOf<String>()
    private val fragmentSavedStates = mutableMapOf<Int,Fragment.SavedState?>()

    fun addFragment(fragment: Fragment, title: String) {
        fragmentsList.add(fragment)
        fragmentsTitleList.add(title)
    }

    fun replaceFragment(position: Int, fragment: Fragment) {
        fragmentsList[position] = fragment
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentsTitleList[position]
    }

    fun setPageTitle(position: Int, str: String){
        fragmentsTitleList[position] = str
    }

    override fun getItem(position: Int): Fragment {
        return fragmentsList[position]
    }

    override fun getCount(): Int {
        return fragmentsList.size
    }
}