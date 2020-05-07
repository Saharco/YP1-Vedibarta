package com.technion.vedibarta.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.Student

import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import kotlin.random.Random

class CharacteristicsFragment : VedibartaFragment() {

    private val TAG = "CharacteristicsFragment"
    private lateinit var argumentTransfer: ArgumentTransfer

    override fun onAttach(context: Context) {
        super.onAttach(context)
        argumentTransfer = context as? ArgumentTransfer
            ?: throw ClassCastException("$context must implement ${ArgumentTransfer::class}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_characteristics, container,
            false
        )
        val table = view.findViewById(R.id.characteristicsTable) as TableLayout
        val argMap = argumentTransfer.getArgs()
        val characteristicsTask = argMap["characteristicsTask"] as Task<MultilingualTextResource>
        val student = argMap["student"] as Student
        val act = argMap["activity"] as Activity
        Tasks.whenAll(characteristicsTask)
            .addOnSuccessListener(act) {
                table.removeAllViews()
                populateCharacteristicsTable(
                    requireContext(),
                    table,
                    characteristicsTask.result!!.getAll().shuffled(Random(42)).toTypedArray(),
                    student.characteristics,
                    characteristicsTask.result!!
                )

            }
        return view
    }

}
