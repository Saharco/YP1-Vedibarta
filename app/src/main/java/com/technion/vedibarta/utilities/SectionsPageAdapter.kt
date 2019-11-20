package com.technion.vedibarta.utilities

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class SectionsPageAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragmentsList = mutableListOf<Fragment>()
    private val fragmentsTitleList = mutableListOf<String>()

    fun addFragment(fragment: Fragment, title: String) {
        fragmentsList.add(fragment)
        fragmentsTitleList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentsTitleList[position]
    }

    override fun getItem(position: Int): Fragment {
        return fragmentsList[position]
    }

    override fun getCount(): Int {
        return fragmentsList.size
    }
}