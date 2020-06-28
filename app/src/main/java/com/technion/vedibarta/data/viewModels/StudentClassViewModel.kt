package com.technion.vedibarta.data.viewModels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.Class
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.POJOs.Teacher
import com.technion.vedibarta.R
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.utilities.VedibartaActivity

class StudentClassViewModel : ViewModel() {

    private val _event = MutableLiveData<Event>()

    val classesList = mutableListOf<Class>()
    val event: LiveData<Event> = _event
    var reloadNeeded = true

    init {
        reloadClasses()
    }

    fun getClassMembers(selectedClass: View) {
        DatabaseVersioning.currentVersion.instance.collection("classes")
            .document(selectedClass.tag.toString())
            .get()
            .addOnSuccessListener {
                val clazz = it.toObject(Class::class.java)!!
                val studentsTask = DatabaseVersioning.currentVersion.instance.collection("students")
                    .whereIn("uid", clazz.studentsIDs)
                    .get()
                val teacherTask = DatabaseVersioning.currentVersion.instance.collection("teachers")
                    .whereEqualTo("uid", clazz.teacherID)
                    .get()
                Tasks.whenAll(studentsTask, teacherTask)
                    .addOnSuccessListener {
                        val members =
                            studentsTask.result!!.documents.map { it.toObject(Student::class.java)!! }
                                .toList()
                        val teacher =
                            teacherTask.result!!.documents.first().toObject(Teacher::class.java)!!
                        _event.value = Event.ClassMembersLoaded(teacher, members)
                    }
                    .addOnFailureListener {
                        _event.value = Event.DisplayError(R.string.something_went_wrong)
                    }
            }
            .addOnFailureListener {
                _event.value = Event.DisplayError(R.string.something_went_wrong)
            }
    }

    fun addStudentToClass(classId: String?) {
        DatabaseVersioning.currentVersion.instance.collection("classes")
            .document(classId!!).get()
            .addOnSuccessListener {
                val clazz = it.toObject(Class::class.java)!!
                if (!classesList.contains(clazz)) {
                    clazz.studentsIDs = clazz.studentsIDs.plus(VedibartaActivity.userId!!)
                    DatabaseVersioning.currentVersion.instance.collection("classes")
                        .document(clazz.id)
                        .update("studentsIDs", clazz.studentsIDs)
                        .addOnSuccessListener {
                            classesList.add(clazz)
                            _event.value = Event.ClassAddedFromInvite()
                        }
                } else {
                    _event.value = Event.AlreadyInClass()
                }
                reloadNeeded = false
            }
    }

    fun reloadClasses() {
        if (reloadNeeded) {
            DatabaseVersioning.currentVersion.instance.collection("classes")
                .whereArrayContains("studentsIDs", VedibartaActivity.userId.toString()).get()
                .addOnSuccessListener {
                    if (it.documents.isNotEmpty()) {
                        classesList.addAll(it.documents.map { it.toObject(Class::class.java)!! }
                            .toList())
                        _event.value = Event.ClassAdded()
                    } else {
                        _event.value = Event.HaveNoClasses()
                    }
                }
                .addOnFailureListener {
                    _event.value = Event.DisplayError(R.string.something_went_wrong)
                }
            reloadNeeded = false
        }
    }

    sealed class Event {
        var handled = false

        data class ClassRemoved(val index: Int) : Event()
        class ClassAdded : Event()
        class ClassAddedFromInvite : Event()
        class HaveNoClasses : Event()
        class AlreadyInClass : Event()
        data class ClassMembersLoaded(
            val teacher: Teacher,
            val members: List<Student>
        ) : Event()

        data class DisplayError(val msgResId: Int) : Event()
    }
}