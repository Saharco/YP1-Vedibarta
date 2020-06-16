package com.technion.vedibarta.data.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.technion.vedibarta.POJOs.Class
import com.technion.vedibarta.POJOs.Filled
import com.technion.vedibarta.POJOs.TextContainer
import com.technion.vedibarta.POJOs.Unfilled
import com.technion.vedibarta.R
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.utilities.VedibartaActivity

class ClassAddViewModel : ViewModel(){

    private val _event = MutableLiveData<ClassAddEvent>()

    //TODO Change from null to default photo
    var chosenClassPicture: String? = null

    var chosenClassDescription: TextContainer = Unfilled
    var chosenClassName: TextContainer = Unfilled
    val event: LiveData<ClassAddEvent> = _event

    fun createClass() {
        val name = when (val name = chosenClassName) {
            is Unfilled -> {
                _event.value =
                    ClassAddEvent.DisplayMissingInfoDialog(R.string.teacher_class_list_missing_class_name); return
            }
            is Filled -> name.text
        }
        val description = when (val desc = chosenClassDescription) {
            is Unfilled -> {
                _event.value =
                    ClassAddEvent.DisplayMissingInfoDialog(R.string.teacher_class_list_missing_class_description); return
            }
            is Filled -> desc.text
        }

        val photo = chosenClassPicture
        val docRef = DatabaseVersioning.currentVersion.instance.collection("classes").document()
        val clazz = Class(
            id = docRef.id,
            name = name,
            teacherID = VedibartaActivity.userId!!,
            description = description,
            photo = photo
        )
        docRef.set(clazz)
            .addOnSuccessListener {
                chosenClassName = Unfilled
                chosenClassDescription = Unfilled
                //TODO change to default photo
                chosenClassPicture = null
                _event.value = ClassAddEvent.ClassAdded(clazz)
            }
            .addOnFailureListener {
                _event.value = ClassAddEvent.DisplayError(R.string.something_went_wrong)
            }
    }

    fun saveEditedClass() {
        val name = when (val name = chosenClassName) {
            is Unfilled -> {
                _event.value =
                    ClassAddEvent.DisplayMissingInfoDialog(R.string.teacher_class_list_missing_class_name); return
            }
            is Filled -> name.text
        }
        val description = when (val desc = chosenClassDescription) {
            is Unfilled -> {
                _event.value =
                    ClassAddEvent.DisplayMissingInfoDialog(R.string.teacher_class_list_missing_class_description); return
            }
            is Filled -> desc.text
        }

        val photo = chosenClassPicture

        val clazzMap = mutableMapOf<String, String?>(
            "name" to name,
            "description" to description,
            "photo" to photo
        )
        _event.value = ClassAddEvent.ClassEdited(clazzMap)


    }

    sealed class ClassAddEvent {
        var handled = false

        data class ClassAdded(val clazz: Class) : ClassAddEvent()
        data class ClassEdited(val clazzMap: Map<String, String?>): ClassAddEvent()
        data class DisplayMissingInfoDialog(val msgResId: Int) : ClassAddEvent()
        data class DisplayError(val msgResId: Int): ClassAddEvent()
    }
}