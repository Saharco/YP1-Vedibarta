package com.technion.vedibarta.userProfile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import com.google.android.gms.tasks.Tasks

import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.student
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import kotlinx.android.synthetic.main.fragment_profile_edit_characteristics.*
import kotlin.random.Random

class ProfileEditCharacteristicsFragment : VedibartaFragment() {

    private val TAG = "CharFragment@Edit"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_profile_edit_characteristics, container,
            false
        )
        val table = view.findViewById(R.id.searchCharacteristics) as TableLayout
        val act = activity as ProfileEditActivity
        Tasks.whenAll(act.characteristicsTask)
            .addOnSuccessListener {
                loading.visibility = View.GONE
                populateCharacteristicsTable(act, table, act.characteristicsTask.result!!.getAll().shuffled(
                    Random(42)
                ).toTypedArray(), student!!, act.characteristicsTask.result!!)

            }
        return view
    }
}
