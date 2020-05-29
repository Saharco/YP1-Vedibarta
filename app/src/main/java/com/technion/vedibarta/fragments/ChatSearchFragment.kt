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
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.CharacteristicsViewModel
import com.technion.vedibarta.data.viewModels.ChatSearchViewModel
import com.technion.vedibarta.data.viewModels.characteristicsViewModelFactory
import com.technion.vedibarta.data.viewModels.chatSearchViewModelFactory
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.matching.StudentsMatcher
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.hideSplash
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.showSplash
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import com.technion.vedibarta.utilities.resourcesManagement.TextResource
import kotlinx.android.synthetic.main.fragment_chat_search.*


class ChatSearchFragment : Fragment() {
    companion object {
        private const val MATCHING_TIMEOUT = 10L
        private const val MINIMUM_TRANSITION_TIME = 900L
        private const val TAG = "ChatSearchActivity"
    }


    private val chatSearchViewModel: ChatSearchViewModel by viewModels {
        chatSearchViewModelFactory(requireActivity().applicationContext)
    }

    private val characteristicsViewModel: CharacteristicsViewModel by activityViewModels {
        characteristicsViewModelFactory(requireActivity().applicationContext, Gender.NONE)
    }

    private lateinit var chatSearchResources: LiveData<LoadableData<ChatSearchResources>>

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatSearchResources = combineResources(
            chatSearchViewModel.schoolsName,
            chatSearchViewModel.regionsName,
            characteristicsViewModel.characteristicsResources
        )


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatSearchResources.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loaded -> {
                    loading.visibility = View.GONE
                    mainLayout.visibility = View.VISIBLE
                }
                is Error -> Toast.makeText(requireContext(), it.reason, Toast.LENGTH_LONG).show()
                is SlowLoadingEvent -> {
                    if (!it.handled)
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.net_error),
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
        })

        toolbar.inflateMenu(R.menu.chat_search_menu)
        toolbar.setOnMenuItemClickListener {
            onMenuItemClick(it)
        }

        viewFlipper.setInAnimation(requireContext(), android.R.anim.fade_in)
        viewFlipper.setOutAnimation(requireContext(), android.R.anim.fade_out)
    }

    override fun onStop() {
        super.onStop()
        if (!VedibartaActivity.splashScreen.isIdleNow)
            VedibartaActivity.splashScreen.decrement()
    }

    private fun onMenuItemClick(it: MenuItem): Boolean {
        when (it.itemId) {
            R.id.actionChatSearch -> {
                //TODO pass the chosen filters to database/other activity fo rmatching
                when(val result = validateChosenDetails()){
                    is Success -> searchMatch()
                    is Failure -> displayErrorMessage(result.msg)
                }
            }
        }
        return true
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

    private fun validateChosenDetails(): SearchResult {
        val resourcesCombo = chatSearchResources.value as? Loaded
            ?: error("tried to validate the user before done loading resources")

        when(val region = chatSearchViewModel.chosenRegion){
            is Filled -> {
                if (!resourcesCombo.data.regionsName.getAll().contains(region.text))
                    return Failure(getString(R.string.chat_search_wrong_region_message))
            }
        }

        when(val school = chatSearchViewModel.chosenSchool){
            is Filled ->{
                if (!resourcesCombo.data.schoolsName.getAll().contains(school.text))
                    return Failure(getString(R.string.chat_search_wrong_school_message))
            }
        }

        if (characteristicsViewModel.chosenCharacteristics.isEmpty())
            return Failure(getString(R.string.chat_search_no_characteristics_chosen_message))

        return Success
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
                val action = ChatSearchFragmentDirections.actionFindFriendsToChatCandidatesActivity(filteredStudents.toTypedArray())
                Handler().postDelayed({
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        findNavController().navigate(action)
                        viewFlipper.showPrevious()
                    }
                }, MINIMUM_TRANSITION_TIME)

            } else {
                Log.d(TAG, "No matching students found")
                Toast.makeText(requireContext(), R.string.no_matching_students, Toast.LENGTH_LONG).show()
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
            Toast.makeText(requireContext(), R.string.something_went_wrong, Toast.LENGTH_LONG).show()
        }

        viewFlipper.showNext()
        showSplash(requireActivity(), getString(R.string.chat_search_loading_message))
    }

    private fun getSearchAttributes(): SearchAttributes {
        val school = when(val school = chatSearchViewModel.chosenSchool){
            is Filled -> school.text
            else -> null
        }
        val region = when(val region = chatSearchViewModel.chosenRegion){
            is Filled -> region.text
            else -> null
        }
        val grade = chatSearchViewModel.grade.takeIf { it != Grade.NONE }
        return SearchAttributes(characteristicsViewModel.chosenCharacteristics, region, school, grade)
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

    private fun combineResources(
        schoolsNameLiveData: LiveData<LoadableData<TextResource>>,
        regionsNameLiveData: LiveData<LoadableData<TextResource>>,
        characteristicsResourcesLiveData: LiveData<LoadableData<CharacteristicsViewModel.CharacteristicsResources>>
    ): LiveData<LoadableData<ChatSearchResources>> {
        val mediator = MediatorLiveData<LoadableData<ChatSearchResources>>()
            .apply { value = NormalLoading() }

        fun refreshCombination() {
            // cannot go back after reaching an end-state
            if (mediator.value is Error) return

            val schoolsName = schoolsNameLiveData.value
            val regionsName = regionsNameLiveData.value
            val characteristicsResources = characteristicsResourcesLiveData.value

            // become Loaded when all resources have been loaded
            if (schoolsName is Loaded
                && regionsName is Loaded
                && characteristicsResources is Loaded
            ) {
                mediator.value = Loaded(
                    ChatSearchResources(
                        schoolsName = schoolsName.data,
                        regionsName = regionsName.data,
                        allCharacteristics = characteristicsResources.data.allCharacteristics,
                        characteristicsCardList = characteristicsResources.data.characteristicsCardList
                    )
                )
                return
            }

            // become Error when the first error occurs
            schoolsName?.let {
                if (it is Error) {
                    mediator.value = Error(it.reason); return
                }
            }
            regionsName?.let {
                if (it is Error) {
                    mediator.value = Error(it.reason); return
                }
            }
            characteristicsResources?.let {
                if (it is Error) {
                    mediator.value = Error(it.reason); return
                }
            }

            // trigger SlowLoadingEvent when the first SlowLoadingEvent occurs
            if (mediator.value !is SlowLoadingEvent
                && (schoolsName is SlowLoadingEvent
                        || regionsName is SlowLoadingEvent
                        || characteristicsResources is SlowLoadingEvent)
            ) {
                mediator.value = SlowLoadingEvent()
                return
            }
        }

        mediator.addSource(schoolsNameLiveData) { refreshCombination() }
        mediator.addSource(regionsNameLiveData) { refreshCombination() }
        mediator.addSource(characteristicsResourcesLiveData) { refreshCombination() }

        return mediator
    }


    data class ChatSearchResources(
        val allCharacteristics: MultilingualTextResource,
        val characteristicsCardList: List<CategoryCard>,
        val schoolsName: TextResource,
        val regionsName: TextResource
    )

    data class SearchAttributes(
        val characteristics: Collection<String>,
        val region: String?,
        val school: String?,
        val grade: Grade?
    )
}
sealed class SearchResult

object Success : SearchResult()

data class Failure(val msg: String) : SearchResult()