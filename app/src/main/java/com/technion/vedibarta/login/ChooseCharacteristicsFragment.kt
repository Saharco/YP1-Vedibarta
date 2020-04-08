package com.technion.vedibarta.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource
import kotlinx.android.synthetic.main.fragment_choose_characteristics.*
import kotlin.random.Random


/**
 * A simple [Fragment] subclass.
 */
class ChooseCharacteristicsFragment : VedibartaFragment() {

    private val TAG = "CharFragment@Setup"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose_characteristics, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val act = activity as UserSetupActivity
        Tasks.whenAll(act.characteristicsMaleTask, act.characteristicsFemaleTask)
            .addOnSuccessListener(act) {
                loading.visibility = View.GONE
                val resource = if (act.setupStudent.gender == Gender.MALE) act.characteristicsMaleTask.result!! else act.characteristicsFemaleTask.result!!
                populateCharacteristicsTable(act, searchCharacteristics, resource.getAll().shuffled(Random(42)).toTypedArray(), act.setupStudent, resource)

            }
    }


}
