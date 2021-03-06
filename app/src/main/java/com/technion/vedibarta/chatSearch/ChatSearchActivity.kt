package com.technion.vedibarta.chatSearch

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.ChatSearchViewModel
import com.technion.vedibarta.fragments.CategorizedBubblesSelectionFragment
import com.technion.vedibarta.utilities.VedibartaActivity

class ChatSearchActivity : VedibartaActivity() {
    companion object {
        private const val TAG = "ChatSearchActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_search)
        Log.d(TAG, "ChatSearchActivity Created")
    }

    override fun onBackPressed() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_content)
        val fragment = navHostFragment!!.childFragmentManager.fragments[0]
        (fragment as? OnBackPressed)?.onBackPressed()?.not()?.let {
            if (it)
                super.onBackPressed()
        }
    }

    interface OnBackPressed {
        fun onBackPressed(): Boolean
    }
}