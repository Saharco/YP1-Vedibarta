package com.technion.vedibarta.teacher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager

import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.BubblesSelectionAdapter
import com.technion.vedibarta.data.viewModels.BubbleViewModel
import com.technion.vedibarta.databinding.FragmentBubblesSelectionBinding
import kotlinx.android.synthetic.main.fragment_bubbles_selection.view.*

val characteristicsList = listOf(
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TeacherCharacteristicsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeacherCharacteristicsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentBubblesSelectionBinding.inflate(inflater, container, false)
        val bubblesViewModels = characteristicsList.map {
            val marked = MutableLiveData(false)

            BubbleViewModel(
                MutableLiveData(it),
                marked
            ) {
                marked.value = !marked.value!!
            }
        }

        val recyclerView = binding.bubblesRecycleView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = BubblesSelectionAdapter(viewLifecycleOwner, bubblesViewModels, false)
        //binding.characteristicCardTitle.text = "זהות בית ספר"

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TeacherCharacteristicsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TeacherCharacteristicsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
