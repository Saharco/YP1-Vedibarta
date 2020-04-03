package com.technion.vedibarta.login


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.HobbyCard
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.HobbiesAdapter
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import kotlinx.android.synthetic.main.fragment_choose_hobbies.*

/**
 * A simple [Fragment] subclass.
 */
class ChooseHobbiesFragment : VedibartaFragment() {

    private val TAG = "HobbiesFragment@login"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_choose_hobbies, container, false)
        val act = (activity as UserSetupActivity)
        Tasks.whenAll(act.hobbiesResourceTask, act.hobbyCardTask)
            .addOnSuccessListener {
                loading.visibility = View.GONE
                val hobbyTitlesList = view.findViewById<RecyclerView>(R.id.hobbyTitlesList)
                hobbyTitlesList.adapter = HobbiesAdapter(act.hobbyCardTask.result!!, (activity as UserSetupActivity).setupStudent, act.hobbiesResourceTask.result!!)
                hobbyTitlesList.layoutManager = LinearLayoutManager(this.context)
            }

        return view
    }


}
