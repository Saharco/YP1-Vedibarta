package com.technion.vedibarta.login


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource
import kotlinx.android.synthetic.main.fragment_choose_characteristics.*
import kotlin.random.Random


/**
 * A simple [Fragment] subclass.
 */
class ChooseCharacteristicsFragment : VedibartaFragment(), UserSetupActivity.OnNextClickForCharacteristics {

    private val TAG = "CharFragment@Setup"
    private lateinit var argumentTransfer: ArgumentTransfer

    private lateinit var characteristicsWithCategoriesMaleTask: Task<Map<String,Array<String>>>
    private lateinit var characteristicsWithCategoriesFemaleTask: Task<Map<String,Array<String>>>
    lateinit var characteristicsMaleTask: Task<MultilingualResource>
    lateinit var characteristicsFemaleTask: Task<MultilingualResource>
    lateinit var setupStudent: Student
    lateinit var act: Activity
    private var currentIndex = 0
    override fun onAttach(context: Context) {
        super.onAttach(context)
        argumentTransfer = context as? ArgumentTransfer
            ?: throw ClassCastException("$context must implement ${ArgumentTransfer::class}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose_characteristics, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStudent = argumentTransfer.getArgs()["student"] as Student
        characteristicsMaleTask = argumentTransfer.getArgs()["characteristicsMaleTask"] as Task<MultilingualResource>
        characteristicsFemaleTask = argumentTransfer.getArgs()["characteristicsFemaleTask"] as Task<MultilingualResource>
        characteristicsWithCategoriesMaleTask = argumentTransfer.getArgs()["characteristicsWithCategoriesMaleTask"] as Task<Map<String,Array<String>>>
        characteristicsWithCategoriesFemaleTask = argumentTransfer.getArgs()["characteristicsWithCategoriesFemaleTask"] as Task<Map<String,Array<String>>>

        currentIndex = 0

        act = argumentTransfer.getArgs()["activity"] as Activity
        Tasks.whenAll(characteristicsMaleTask, characteristicsFemaleTask, characteristicsWithCategoriesMaleTask, characteristicsWithCategoriesFemaleTask)
            .addOnSuccessListener(act) {
                val resource = if (setupStudent.gender == Gender.MALE) characteristicsMaleTask.result!! else characteristicsFemaleTask.result!!
                val characteristics = if (setupStudent.gender == Gender.MALE) characteristicsWithCategoriesMaleTask.result!! else characteristicsWithCategoriesFemaleTask.result!!
                characteristicsTitle.text = characteristics.keys.toList().first()
                loadCharacteristics(characteristics[characteristics.keys.toList().first()]?: emptyArray(), resource)
            }
    }

    private fun loadCharacteristics(
        characteristics: Array<String>,
        resource: MultilingualResource
    ) {
        characteristicsTable.removeAllViews()
        populateCharacteristicsTable(context!!, characteristicsTable, characteristics.toList().shuffled(Random(42)).toTypedArray(), setupStudent, resource)
    }

    override fun onNextClick(): Task<Boolean> {
        return Tasks.whenAll(characteristicsMaleTask, characteristicsFemaleTask, characteristicsWithCategoriesMaleTask, characteristicsWithCategoriesFemaleTask)
            .continueWith {
                currentIndex++
                if (currentIndex < characteristicsWithCategoriesMaleTask.result!!.keys.size){
                    val resource = if (setupStudent.gender == Gender.MALE) characteristicsMaleTask.result!! else characteristicsFemaleTask.result!!
                    val characteristics = if (setupStudent.gender == Gender.MALE) characteristicsWithCategoriesMaleTask.result!! else characteristicsWithCategoriesFemaleTask.result!!
                    characteristicsTitle.text = characteristics.keys.toList()[currentIndex]
                    loadCharacteristics(characteristics[characteristics.keys.toList()[currentIndex]]?: emptyArray(), resource)
                    return@continueWith false
                }
                return@continueWith true
            }
    }

    override fun onBackClick(): Task<Boolean> {
        return Tasks.whenAll(characteristicsMaleTask, characteristicsFemaleTask, characteristicsWithCategoriesMaleTask, characteristicsWithCategoriesFemaleTask)
            .continueWith {
                currentIndex--
                if (currentIndex > 0){
                    val resource = if (setupStudent.gender == Gender.MALE) characteristicsMaleTask.result!! else characteristicsFemaleTask.result!!
                    val characteristics = if (setupStudent.gender == Gender.MALE) characteristicsWithCategoriesMaleTask.result!! else characteristicsWithCategoriesFemaleTask.result!!
                    characteristicsTitle.text = characteristics.keys.toList()[currentIndex]
                    loadCharacteristics(characteristics[characteristics.keys.toList()[currentIndex]]?: emptyArray(), resource)
                    return@continueWith false
                }
                return@continueWith true
            }
    }
}
