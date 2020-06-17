package com.technion.vedibarta.teacher

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import com.technion.vedibarta.POJOs.Loaded
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.ChatSearchViewModel
import com.technion.vedibarta.fragments.CategorizedBubblesSelectionFragment
import com.technion.vedibarta.utilities.VedibartaActivity

// This class is currently identical to ChatSearchActivity class
class TeacherSearchActivity : VedibartaActivity() {
    companion object {
        private const val TAG = "TeacherSearchActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_search)
        Log.d(TAG, "TeacherSearchActivity Created")

    }

//    override fun onBackPressed() {
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.teacher_search_main_content)
//        val fragment = navHostFragment!!.childFragmentManager.fragments[0]
//        (fragment as? OnBackPressed)?.onBackPressed()?.not()?.let {
//            if (it)
//                super.onBackPressed()
//        }
//    }

    interface OnBackPressed {
        fun onBackPressed(): Boolean
    }
}