package com.technion.vedibarta.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentListStateAdapter(fragmentActivity: FragmentActivity, list: List<() -> Fragment> = emptyList()) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = list.toMutableList()

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]()

    fun addFragment(position: Int, fragment: () -> Fragment){
        fragmentList.add(position, fragment)
        this.notifyDataSetChanged()
    }

    fun addFragment(fragment: ()-> Fragment){
        fragmentList.add(fragment)
        this.notifyDataSetChanged()
    }

    fun removeFragment(position: Int){
        fragmentList.removeAt(position)
        this.notifyDataSetChanged()
    }
}