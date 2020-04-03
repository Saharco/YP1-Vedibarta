package com.technion.vedibarta.chatSearch


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import kotlinx.android.synthetic.main.fragment_search_characteristics.*
import kotlin.random.Random

/**
 * A simple [Fragment] subclass.
 */
class SearchCharacteristicsFragment : VedibartaFragment() {

    private val TAG = "CharFragment@Search"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_characteristics, container, false)
        val act = (activity as ChatSearchActivity)

        Tasks.whenAll(act.characteristicsTask)
            .addOnSuccessListener {
                loading.visibility = View.GONE
                populateCharacteristicsTable(act, searchCharacteristics, act.characteristicsTask.result!!.getAll().shuffled(
                    Random(42)
                ).toTypedArray(), act.fakeStudent, act.characteristicsTask.result!!)

            }
        return view
    }
}
