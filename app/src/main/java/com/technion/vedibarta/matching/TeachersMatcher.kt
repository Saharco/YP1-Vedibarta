package com.technion.vedibarta.matching

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.technion.vedibarta.POJOs.Teacher
import com.technion.vedibarta.POJOs.Timetable
import com.technion.vedibarta.database.DatabaseVersioning

const val TEACHERS_LIMIT = 10L
val DEFAULT_TEACHERS_COLLECTION: CollectionReference = DatabaseVersioning.currentVersion.instance.collection("teachers")


class TeachersMatcher(teachersCollection: CollectionReference = DEFAULT_TEACHERS_COLLECTION) {
    private val matcher = DocumentsMatcher.ofCollection(teachersCollection)

    fun match(
        characteristics: Set<String>,
        grades: Teacher.Grades? = null,
        subjects: Set<String>? = null,
        schedule: Timetable? = null,
        region: String? = null,
        school: String? = null,
        limit: Long = TEACHERS_LIMIT
    ): Task<List<Teacher>> {
        var matcher = matcher

        if (region != null) matcher = matcher.whereFieldsMatch("region" to region)
        if (school != null) matcher = matcher.whereFieldsMatch("school" to school)

        matcher = matcher.whereFieldsMatch(characteristics.map { "characteristics.$it" to true }.toMap())

        if (grades != null)
            matcher = matcher.whereFieldsMatch(grades.toList().map { "grades.$it" to true }.toMap())

        if (subjects != null)
            matcher = matcher.whereFieldsMatch(subjects.map { "teachingSubjects.$it" to true }.toMap())

        if (schedule != null)
            matcher = matcher.whereAtLeastOneFieldMatch(schedule.toStringMap().mapKeys { "timetable.${it.key}" })

        return matcher.match(randomField = "uid", limit = limit).continueWith { task ->
            task.result!!.map { it.toObject(Teacher::class.java)!! }
        }
    }
}
