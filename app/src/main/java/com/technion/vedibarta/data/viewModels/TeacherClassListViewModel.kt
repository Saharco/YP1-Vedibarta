package com.technion.vedibarta.data.viewModels

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.technion.vedibarta.POJOs.Class
import com.technion.vedibarta.POJOs.Filled
import com.technion.vedibarta.POJOs.TextContainer
import com.technion.vedibarta.POJOs.Unfilled
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.ClassMembersListAdapter
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.missingDetailsDialog
import kotlinx.android.synthetic.main.fragment_teacher_classes_list.*

class TeacherClassListViewModel : ViewModel() {

    private val _event = MutableLiveData<Event>()


    var itemActionBarEnabled = false
    var selectedItems = 0
    val selectedItemsList = mutableListOf<MaterialCardView>()

    //TODO Make Listener that will update classesList according to changes from firebase?
    val classesList = mutableListOf<Class>()
    val event: LiveData<Event> = _event

    init {
        DatabaseVersioning.currentVersion.instance.collection("classes")
            .whereEqualTo("teacherID", VedibartaActivity.userId).get()
            .addOnSuccessListener {
                if (it.documents.isNotEmpty()) {
                    classesList.addAll(it.documents.map { it.toObject(Class::class.java)!! }
                        .toList())
                    _event.value = Event.ClassAdded()
                }
            }
            .addOnFailureListener {
                _event.value = Event.DisplayError(R.string.something_went_wrong)
            }
    }

    fun handleOnBackPress(): Boolean {
        if (itemActionBarEnabled) {
            clearSelectedClasses()
            return true
        }
        return false
    }

    private fun clearSelectedClasses() {
        itemActionBarEnabled = false
        selectedItems = 0
        selectedItemsList.forEach {
            it.isLongClickable = true
            it.isChecked = false
        }
        selectedItemsList.clear()
        _event.value = Event.ToggleActionBar()
    }

    fun unSelectClass(v: MaterialCardView) {
        selectedItems--
        selectedItemsList.remove(v)
        v.isLongClickable = true
        v.isChecked = false
        if (selectedItems == 0) {
            itemActionBarEnabled = false
            _event.value = Event.ToggleActionBar()
        } else {
            _event.value = Event.UpdateTitle()
        }
    }


    fun beginClassExtraActions(v: MaterialCardView) {
        itemActionBarEnabled = true
        _event.value = Event.ToggleActionBar()
        selectClass(v)
    }

    fun selectClass(v: MaterialCardView) {
        selectedItems++
        selectedItemsList.add(v)
        v.isLongClickable = false
        v.isChecked = true
        _event.value = Event.UpdateTitle()
    }

    fun removeSelectedClasses() {
        selectedItemsList.forEach {
            val id = it.tag
            val classToRemove: Class = classesList.first { it.id == id }
            val removeIndex = classesList.indexOf(classToRemove)
            DatabaseVersioning.currentVersion.instance.collection("classes").document(id.toString())
                .delete()
                .addOnSuccessListener {
                    _event.value = Event.ClassRemoved(removeIndex + 1)
                    classesList.remove(classToRemove)
                }
                .addOnFailureListener {
                    _event.value = Event.DisplayError(R.string.something_went_wrong)
                }
        }
        selectedItemsList.clear()
        selectedItems = 0
        itemActionBarEnabled = false
        _event.value = Event.ToggleActionBar()
    }

    fun addClass(clazz: Class) {
        classesList.add(clazz)
        _event.value = Event.ClassAdded()
    }

    fun editClass(clazzMap: Map<String, String?>) {
        val classToEdit: Class = classesList.first { it.id == selectedItemsList.first().tag }
        val editIndex = classesList.indexOf(classToEdit)
        classesList[editIndex].name = clazzMap["name"]!!
        classesList[editIndex].description = clazzMap["description"]!!
        classesList[editIndex].photo = clazzMap["photo"]
        DatabaseVersioning.currentVersion.instance.collection("classes").document(classToEdit.id)
            .update(clazzMap).addOnSuccessListener {
                _event.value = Event.ClassEdited(editIndex+1)
                unSelectClass(selectedItemsList.first())
            }
            .addOnFailureListener {
                _event.value = Event.DisplayError(R.string.something_went_wrong)
            }
    }

    sealed class Event {
        var handled = false

        class ToggleActionBar : Event()
        class UpdateTitle : Event()
        data class ClassRemoved(val index: Int) : Event()
        data class ClassEdited(val index: Int) : Event()
        class ClassAdded : Event()
        data class DisplayError(val msgResId: Int) : Event()
    }

}