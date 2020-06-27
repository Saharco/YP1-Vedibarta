package com.technion.vedibarta.data.viewModels

import android.net.Uri
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.card.MaterialCardView
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.technion.vedibarta.POJOs.Class
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.utilities.VedibartaActivity
import java.nio.charset.Charset


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


    private fun editWithClassPhoto(
        classToEdit: Class,
        clazzMap: Map<String, String?>,
        bytes: ByteArray,
        editIndex: Int
    ) {
        val storageRef =
            FirebaseStorage.getInstance().reference.child("classes/${classToEdit.id}/photo")
        storageRef.putBytes(bytes)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener {
                        DatabaseVersioning.currentVersion.instance.collection("classes")
                            .document(classToEdit.id)
                            .update(clazzMap).addOnSuccessListener {
                                _event.value = Event.ClassEdited(editIndex + 1)
                                unSelectClass(selectedItemsList.first())
                            }
                            .addOnFailureListener {
                                _event.value = Event.DisplayError(R.string.something_went_wrong)
                            }
                    }
            }
    }

    private fun editWithoutClassPhoto(
        classToEdit: Class,
        clazzMap: Map<String, String?>,
        editIndex: Int
    ) {
        DatabaseVersioning.currentVersion.instance.collection("classes")
            .document(classToEdit.id)
            .update(clazzMap).addOnSuccessListener {
                _event.value = Event.ClassEdited(editIndex + 1)
                unSelectClass(selectedItemsList.first())
            }
            .addOnFailureListener {
                _event.value = Event.DisplayError(R.string.something_went_wrong)
            }
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

    fun handleOnBackPress(): Boolean {
        if (itemActionBarEnabled) {
            clearSelectedClasses()
            return true
        }
        return false
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
            DatabaseVersioning.currentVersion.instance.collection("classes")
                .document(id.toString())
                .delete()
                .addOnSuccessListener {
                    classesList.remove(classToRemove)
                    if (classToRemove.photo != null) {
                        FirebaseStorage.getInstance().reference.child("classes/${classToRemove.id}/photo")
                    }
                    _event.value = Event.ClassRemoved(removeIndex + 1)
                }
                .addOnFailureListener {
                    _event.value = Event.DisplayError(R.string.something_went_wrong)
                }
        }
        clearSelectedClasses()
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
        val bytes = clazzMap.getValue("photoBytes")!!.toByteArray(Charset.defaultCharset())
        val map = clazzMap.toMutableMap()
        map.remove("photoBytes")
        if (classesList[editIndex].photo != null)
            editWithClassPhoto(classToEdit, map, bytes, editIndex)
        else
            editWithoutClassPhoto(classToEdit, map, editIndex)
    }

    fun getClassMembers(selectedClass: View){
        DatabaseVersioning.currentVersion.instance.collection("classes")
            .document(selectedClass.tag.toString())
            .get()
            .addOnSuccessListener {
                val clazz = it.toObject(Class::class.java)
                if (clazz!!.studentsIDs.isNotEmpty())
                    DatabaseVersioning.currentVersion.instance.collection("students")
                        .whereIn("uid", clazz.studentsIDs)
                        .get()
                        .addOnSuccessListener {
                            val members =
                                it.documents.map { it.toObject(Student::class.java)!! }.toList()
                            _event.value = Event.ClassMembersLoaded(members)
                        }
                        .addOnFailureListener {
                            _event.value = Event.DisplayError(R.string.something_went_wrong)
                        }
                else{
                    _event.value = Event.ClassMembersLoaded(emptyList())
                }
            }
            .addOnFailureListener {
                _event.value = Event.DisplayError(R.string.something_went_wrong)
            }
    }

    fun createClassInvite(v: View){
        Firebase.dynamicLinks.shortLinkAsync(ShortDynamicLink.Suffix.SHORT){
            link = Uri.parse("https://vedibarta.page.link/?classID=${v.tag}")
            domainUriPrefix = "https://vedibarta.page.link"
            androidParameters {  }
            this.buildShortDynamicLink()
        }
            .addOnSuccessListener {
                _event.value = Event.ClassInviteCreated(it.shortLink!!)
            }
    }


    sealed class Event {
        var handled = false

        class ToggleActionBar : Event()
        class UpdateTitle : Event()
        data class ClassRemoved(val index: Int) : Event()
        data class ClassEdited(val index: Int) : Event()
        class ClassAdded : Event()
        data class ClassInviteCreated(val link:Uri): Event()
        data class ClassMembersLoaded(val members: List<Student>) : Event()
        data class DisplayError(val msgResId: Int) : Event()
    }
}