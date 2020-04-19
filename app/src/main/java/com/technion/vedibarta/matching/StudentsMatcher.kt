package com.technion.vedibarta.matching

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.database.DatabaseVersioning


const val STUDENTS_LIMIT = 10L
val DEFAULT_STUDENTS_COLLECTION: CollectionReference = DatabaseVersioning.currentVersion.instance.collection("students")

/**
 * A class that implements the students matching algorithm.
 *
 * The returned set of students will contain only perfect matches (see definition below). In the
 * case that there aren't enough perfect matches, the rest of the students will be ok matches.
 *
 * Any student which fits the required characteristics, region and school will be considered a
 * perfect match. If there aren't enough students who fit the parameters (less than the wanted
 * amount), any other student who has all-but-one of the specified characteristics, will be
 * considered an ok match.
 *
 * Use the method [match] in order to get the students that fits the criteria.
 *
 * @param studentsCollection a reference to the students collection where matches will be searched;
 *  defaults to [DEFAULT_STUDENTS_COLLECTION]
 */
class StudentsMatcher(studentsCollection: CollectionReference = DEFAULT_STUDENTS_COLLECTION) {
    private val matcher = DocumentsMatcher.ofCollection(studentsCollection)

    /**
     * The algorithm will return no more than [STUDENTS_LIMIT] students by default.
     *
     * @param characteristics the wanted set of characteristics, which the matched users must have
     * @param region if not null, the region in which the matched users must live
     * @param school if not null, the school in which the matched users must study
     * @param limit the max amount of returned students; defaults to [STUDENTS_LIMIT]
     */
    fun match(
        characteristics: Collection<String>,
        region: String? = null,
        school: String? = null,
        limit: Long = STUDENTS_LIMIT
    ): Task<List<Student>> {
        var matcher = matcher

        if (region != null) matcher = matcher.whereFieldsMatch("region" to region)
        if (school != null) matcher = matcher.whereFieldsMatch("school" to school)

        matcher = if (characteristics.size > 1) {
            matcher.whereAtLeastOneFieldMatch(characteristics.map { "characteristics.$it" to true }.toMap())
        } else {
            matcher.whereFieldsMatch(characteristics.map { "characteristics.$it" to true }.toMap())
        }

        return matcher.match(randomField = "uid", limit = limit).continueWith { task ->
            task.result!!.map { it.toObject(Student::class.java)!! }
        }
    }
}