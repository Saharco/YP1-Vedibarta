package com.technion.vedibarta.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import kotlinx.android.synthetic.main.fragment_choose_characteristics.*
import kotlin.random.Random


/**
 * A simple [Fragment] subclass.
 */
class ChooseCharacteristicsFragment : VedibartaFragment() {

    private val TAG = "CharFragment@Setup"
    private lateinit var resourcesManager: RemoteResourcesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_characteristics, container, false)
    }

    override fun onStart() {
        super.onStart()
        resourcesManager = RemoteResourcesManager(context!!)
        resourcesManager.findMultilingualResource("characteristics")
            .addOnSuccessListener {
                populateCharacteristicsTable(activity as UserSetupActivity, searchCharacteristics, it.getAll().shuffled(Random(42)).toTypedArray(), (activity as UserSetupActivity).setupStudent)
            }

//        val characteristics = if ((activity as UserSetupActivity).setupStudent.gender != Gender.FEMALE)
//            resources.getStringArray(R.array.characteristicsMale)
//        else
//            resources.getStringArray(R.array.characteristicsFemale)

//        populateCharacteristicsTable(activity as UserSetupActivity, searchCharacteristics, characteristics.toList().shuffled().toTypedArray(), (activity as UserSetupActivity).setupStudent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resourcesManager.findResource("characteristics")
            .addOnSuccessListener { it.close() }
    }

}
