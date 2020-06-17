package com.technion.vedibarta.teacher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import com.technion.vedibarta.POJOs.Bubble

import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.BubblesSelectionAdapter
import com.technion.vedibarta.data.viewModels.BubbleViewModel
import com.technion.vedibarta.data.viewModels.BubblesSelectionViewModel
import com.technion.vedibarta.data.viewModels.BubblesSelectionViewModelFactory
import com.technion.vedibarta.databinding.FragmentBubblesSelectionBinding
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import com.technion.vedibarta.utilities.resourcesManagement.toCurrentLanguage
import kotlinx.android.synthetic.main.fragment_bubbles_selection.view.*
import java.lang.IllegalArgumentException

private var DISPLAY_MODE = SCHOOL_CHARACTERISTICS

const val schoolCharacteristicsTitle = "זהות בית הספר"
val schoolCharacteristicsList = arrayListOf(
    "ממלכתי",
    "ממלכתי-דתי",
    "ממלכתי-דרוזי",
    "ממלכתי-דתי-בדואי",
    "אנתרופוסופי",
    "אולפנה",
    "דמוקרטי",
    "חינוך מיוחד",
    "כנסייתי",
    "דו לשוני"
)

const val teacherSubjectsTitle = "אני מלמד/ת את המקצועות"
val teacherSubjectsList = arrayListOf(
    "אזרחות",
    "חינוך",
    "עברית",
    "אנגלית",
    "צרפתית",
    "ערבית"
)

sealed class TeacherCharacteristicsData(val title: String, val list: ArrayList<String>)
data class SchoolCharacteristicsData(
    val sc_title: String = schoolCharacteristicsTitle,
    val sc_list: ArrayList<String> = schoolCharacteristicsList): TeacherCharacteristicsData(sc_title, sc_list)
data class TeacherSubjectsData(
    val ts_title: String = teacherSubjectsTitle,
    val ts_list: ArrayList<String> = teacherSubjectsList): TeacherCharacteristicsData(ts_title, ts_list)

/**
 * A simple [Fragment] subclass.
 * Use the [TeacherCharacteristicsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeacherCharacteristicsFragment : Fragment() {
    private var title: String = ""
    private var bubbles: ArrayList<Bubble> = arrayListOf()
    private var translator = MutableLiveData<MultilingualTextResource>() // TODO: Change this dummy to a real translator

    private val viewModel: BubblesSelectionViewModel by viewModels {
        BubblesSelectionViewModelFactory(
            translator,
            title,
            bubbles
        )
    }

    private var _binding: FragmentBubblesSelectionBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            var displayDataClass: TeacherCharacteristicsData = schoolCharacteristicsData
            if (DISPLAY_MODE == TEACHER_SUBJECTS) {
                displayDataClass = teacherSubjectsData
            }
            title = displayDataClass.title
            val bubblesStrings = displayDataClass.list
            bubblesStrings.forEach { string -> bubbles.add(Bubble(string)) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBubblesSelectionBinding.inflate(inflater, container, false)
//        val bubblesViewModels = bubbles.map {
//            val marked = MutableLiveData(false)
//
//            BubbleViewModel(
//                MutableLiveData(it),
//                marked
//            ) {
//                marked.value = !marked.value!!
//            }
//        }
//
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        val recyclerView = binding.bubblesRecycleView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = BubblesSelectionAdapter(viewLifecycleOwner, viewModel.bubbleViewModels, false)

    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    companion object {
        val schoolCharacteristicsData = SchoolCharacteristicsData()
        val teacherSubjectsData = TeacherSubjectsData()

        @JvmStatic
        fun newInstance(mode: String) =
            TeacherCharacteristicsFragment().apply {
                arguments = Bundle().apply {
                    putString(DISPLAY_MODE, mode)
                }
            }
    }
}
