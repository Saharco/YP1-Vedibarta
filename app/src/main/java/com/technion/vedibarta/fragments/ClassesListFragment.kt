package com.technion.vedibarta.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.technion.vedibarta.R
import com.technion.vedibarta.main.MainActivity
import kotlinx.android.synthetic.main.fragment_chat_list.*
import kotlinx.android.synthetic.main.fragment_classes_list.*
import kotlinx.android.synthetic.main.fragment_classes_list.searchView
import kotlinx.android.synthetic.main.fragment_classes_list.toolbar


class ClassesListFragment : Fragment(), MainActivity.OnBackPressed {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_classes_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.inflateMenu(R.menu.chat_list_menu)
        searchView.setMenuItem(toolbar.menu.findItem(R.id.search))
    }

    override fun onBackPressed(): Boolean {
        if (searchView.isShown) {
            searchView.closeSearch()
            return true
        }
        return false
    }

}
