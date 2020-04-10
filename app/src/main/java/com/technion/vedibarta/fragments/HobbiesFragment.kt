package com.technion.vedibarta.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.HobbyCard
import com.technion.vedibarta.POJOs.Student

import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.HobbiesAdapter
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource
import kotlinx.android.synthetic.main.fragment_hobbies.*

/**
 * A simple [Fragment] subclass.
 */
class HobbiesFragment : VedibartaFragment() {

    private val TAG = "HobbiesFragment"

    private lateinit var argumentTransfer: ArgumentTransfer

    override fun onAttach(context: Context) {
        super.onAttach(context)
        argumentTransfer = context as? ArgumentTransfer ?:
                throw ClassCastException("$context must implement ${ArgumentTransfer::class}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hobbies, container, false)

        val argMap = argumentTransfer.getArgs()
        val hobbiesResourceTask = argMap["hobbiesResourceTask"] as Task<MultilingualResource>
        val hobbyCardTask = argMap["hobbyCardTask"] as Task<List<HobbyCard>>
        val student = argMap["student"] as Student
        val act = argMap["activity"] as Activity
        Tasks.whenAll(hobbiesResourceTask, hobbyCardTask)
            .addOnSuccessListener(act) {
                val hobbyTitlesList = view.findViewById<RecyclerView>(R.id.hobbyTitlesList)
                hobbyTitlesList.adapter = HobbiesAdapter(hobbyCardTask.result!!,  student, hobbiesResourceTask.result!!)
                hobbyTitlesList.layoutManager = LinearLayoutManager(context)
            }

        return view
    }

}
