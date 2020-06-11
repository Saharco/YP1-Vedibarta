package com.technion.vedibarta.fragments

import android.content.Context
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.FragmentListStateAdapter
import com.technion.vedibarta.chatSearch.ChatSearchActivity
import com.technion.vedibarta.chatSearch.SearchExtraOptionsFragment
import com.technion.vedibarta.data.viewModels.ChatSearchViewModel
import com.technion.vedibarta.matching.StudentsMatcher
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.hideSplash
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.showSplash
import kotlinx.android.synthetic.main.fragment_chat_search.*

class ChatSearchFragment : Fragment(), ChatSearchActivity.OnBackPressed {

    companion object {
        private const val MATCHING_TIMEOUT = 10L
        private const val MINIMUM_TRANSITION_TIME = 900L
        private const val TAG = "ChatSearchActivity"
    }

    private val viewModel: ChatSearchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setToolbar(toolbar)

        searchButton.setOnClickListener {
            viewModel.searchPressed()
        }

        setupViewPager()

        viewModel.event.observe(viewLifecycleOwner) {
            if (it.handled)
                return@observe

            when (it) {
                is ChatSearchViewModel.Event.Search -> searchMatch()
                is ChatSearchViewModel.Event.DisplayFailure -> displayErrorMessage(getString(it.msgResId))
            }

            it.handled = true
        }

        viewFlipper.setInAnimation(requireContext(), android.R.anim.fade_in)
        viewFlipper.setOutAnimation(requireContext(), android.R.anim.fade_out)
    }

    override fun onStop() {
        super.onStop()
        if (!VedibartaActivity.splashScreen.isIdleNow)
            VedibartaActivity.splashScreen.decrement()
    }

    private fun setToolbar(tb: Toolbar) {
        (activity as AppCompatActivity).setSupportActionBar(tb)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> requireActivity().onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViewPager() {
        searchUserContainer.isUserInputEnabled = true

        val fragments = listOf({
            ChatSearchCharacteristicsSelectionFragment()
        }, {
            SearchExtraOptionsFragment()
        })
        searchUserContainer.adapter = FragmentListStateAdapter(requireActivity(), fragments)
        TabLayoutMediator(editTabs, searchUserContainer) { tab, position ->
            tab.text = "${position + 1}"
        }.attach()
    }

    private fun displayErrorMessage(message: String) {
        val title = TextView(requireContext())
        title.setText(R.string.chat_search_wrong_details_title)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary))
        title.gravity = Gravity.CENTER
        title.setPadding(10, 40, 10, 24)
        val builder = AlertDialog.Builder(requireContext())
        builder.setCustomTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.yes) { _, _ -> }
            .show()
        builder.create()
    }

    private fun searchMatch() {
        Log.d(TAG, "Searching a match")
        val attributes: SearchAttributes = getSearchAttributes()

        StudentsMatcher().match(
            attributes.characteristics,
            attributes.region,
            attributes.school,
            attributes.grade
        ).addOnSuccessListener(requireActivity()) { students ->
            val filteredStudents = students.filter { it.uid != VedibartaActivity.student!!.uid }
            if (filteredStudents.isNotEmpty()) {
                Log.d(TAG, "Matched students successfully")
                val action =
                    ChatSearchFragmentDirections.actionChatSearchFragmentToChatCandidatesActivity(
                        filteredStudents.toTypedArray()
                    )
                Handler().postDelayed({
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        findNavController().navigate(action)
                        viewFlipper.showPrevious()
                    }
                }, MINIMUM_TRANSITION_TIME)

            } else {
                Log.d(TAG, "No matching students found")
                Toast.makeText(requireContext(), R.string.no_matching_students, Toast.LENGTH_LONG)
                    .show()
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    hideSplash(requireActivity())
                    viewFlipper.showPrevious()
                }
            }
        }.addOnFailureListener(requireActivity()) { exp ->
            Log.w(TAG, "Matching students failed", exp)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                if (isInternetAvailable(requireContext())) {
                    //TODO: retry search!
                }
            }
            viewFlipper.showPrevious()
            Toast.makeText(requireContext(), R.string.something_went_wrong, Toast.LENGTH_LONG)
                .show()
        }

        viewFlipper.showNext()
        showSplash(requireActivity(), getString(R.string.chat_search_loading_message))
    }

    private fun getSearchAttributes(): SearchAttributes {
        val school = when (val school = viewModel.chosenSchool) {
            is Filled -> school.text
            else -> null
        }

        val region = when (val region = viewModel.chosenRegion) {
            is Filled -> region.text
            else -> null
        }

        val grade = when (val grade = viewModel.grade) {
            Grade.NONE -> null
            else -> grade
        }

        return SearchAttributes(viewModel.selectedCharacteristics, region, school, grade)
    }

    @Suppress("DEPRECATION")
    private fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = true
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = true
                    }
                }
            }
        }
        return result
    }

    data class SearchAttributes(
        val characteristics: Collection<String>,
        val region: String?,
        val school: String?,
        val grade: Grade?
    )

    override fun onBackPressed(): Boolean {
        return false
    }
}