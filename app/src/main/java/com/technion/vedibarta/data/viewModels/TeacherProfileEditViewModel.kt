package com.technion.vedibarta.data.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.technion.vedibarta.POJOs.DayHour
import com.technion.vedibarta.POJOs.Teacher
import com.technion.vedibarta.POJOs.toMutableTimetable
import com.technion.vedibarta.R
import com.technion.vedibarta.data.TeacherMeta
import com.technion.vedibarta.utilities.VedibartaActivity

class TeacherProfileEditViewModel : ViewModel() {

    private val _event = MutableLiveData<Event>()

    private val initialCharacteristics = TeacherMeta.teacher.schoolCharacteristics.keys
    private val initialSubjects = TeacherMeta.teacher.teachingSubjects.keys
    val initialSchedule = TeacherMeta.teacher.getSchedule()

    private fun changesOccurred() =
        characteristics != initialCharacteristics
            || subjects != initialSubjects
            || !schedule.isEqualsTo(initialSchedule)

    var characteristics = initialCharacteristics
    var subjects = initialSubjects
    private val schedule = initialSchedule.toMutableTimetable()

    fun scheduleTimeChanged(time: DayHour, isPressed: Boolean) {
        if (isPressed)
            schedule.add(time)
        else
            schedule.remove(time)
    }

    val event: LiveData<Event> = _event

    fun backPressed() {
        _event.value = if (!changesOccurred()) {
            Event.Finish.Cancel()
        } else {
            Event.DisplayConfirmationDialog()
        }
    }

    fun confirmationCancelPressed() {
        _event.value = Event.Finish.Cancel()
    }

    fun commitChangesPressed() {
        if (!changesOccurred()) {
            _event.value = Event.Finish.Cancel()
            return
        }

        val initialTeacher = TeacherMeta.teacher

        TeacherMeta.teacher = Teacher(
            initialTeacher.name,
            initialTeacher.photo,
            initialTeacher.gender,
            initialTeacher.uid,
            initialTeacher.regions,
            initialTeacher.schools,
            characteristics.map { it to true }.toMap(),
            initialTeacher.grades,
            subjects.map { it to true }.toMap(),
            schedule
        )

        VedibartaActivity.database.teachers().user().build().set(TeacherMeta.teacher).addOnSuccessListener {
            _event.value = Event.Finish.Success()
        }.addOnFailureListener {
            TeacherMeta.teacher = initialTeacher

            _event.value = Event.DisplayError(R.string.something_went_wrong)
        }
    }

    sealed class Event {
        var handled = false

        class DisplayConfirmationDialog : Event()
        data class DisplayError(val errorMsgId: Int) : Event()

        sealed class Finish : Event() {
            class Cancel : Finish()
            class Success : Finish()
        }
    }
}