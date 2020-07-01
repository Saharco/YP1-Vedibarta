package com.technion.vedibarta.matching

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.technion.vedibarta.POJOs.Grade
import com.technion.vedibarta.POJOs.Teacher
import com.technion.vedibarta.POJOs.Timetable
import com.technion.vedibarta.database.DatabaseVersioning
import java.util.*

const val TEACHERS_LIMIT = 10L
val DEFAULT_TEACHERS_COLLECTION: CollectionReference = DatabaseVersioning.currentVersion.instance.collection("teachers")


class TeachersMatcher(teachersCollection: CollectionReference = DEFAULT_TEACHERS_COLLECTION) {
    private val matcher = DocumentsMatcher.ofCollection(teachersCollection)

    fun match(
        characteristics: Set<String>,
        grade: Grade? = null,
        subjects: Set<String>? = null,
        schedule: Timetable? = null,
        region: String? = null,
        school: String? = null,
        limit: Long = TEACHERS_LIMIT
    ): Task<List<Teacher>> {
        var matcher = matcher

        if (region != null && school == null)
            matcher = matcher.whereArrayFieldContains("regions" to region)
        if (school != null)
            matcher = matcher.whereArrayFieldContains("schools" to school)

        matcher = matcher.whereFieldsMatch(characteristics.map { "schoolCharacteristics.$it" to true }.toMap())

        if (grade != null)
            matcher = matcher.whereFieldsMatch("grades.${grade.toString().toLowerCase(Locale.ROOT)}" to true)

        if (subjects != null)
            matcher = matcher.whereFieldsMatch(subjects.map { "teachingSubjects.$it" to true }.toMap())

        if (schedule != null)
            matcher = matcher.whereAtLeastOneFieldMatch(schedule.toStringMap().mapKeys { "timetable.${it.key}" })

        return matcher.match(randomField = "uid", limit = limit).continueWith { task ->
            task.result!!.map { it.toObject(Teacher::class.java)!! }
        }
    }
}
