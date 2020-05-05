package com.technion.vedibarta.POJOs

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

sealed class User(
    open val name: String,
    open val photo: String?,
    open val gender: Gender,
    open val uid: String
): Serializable, Parcelable


@Parcelize
data class Student(
    override var name: String = "",
    override var photo: String? = null,
    override var gender: Gender = Gender.NONE,
    override val uid: String = "",
    var region: String = "",
    var school: String = "",
    var grade: Grade = Grade.NONE,
    var characteristics: MutableMap<String, Boolean> = mutableMapOf(),
    var hobbies: List<String> = emptyList()
): User(name, photo, gender, uid)


@Parcelize
data class Teacher
internal constructor(
    override val name: String = "",
    override val photo: String? = null,
    override val gender: Gender = Gender.NONE,
    override val uid: String = "",
    val regions: List<String> = emptyList(),
    val schools: List<String> = emptyList(),
    val schoolCharacteristics: Map<String, Boolean> = emptyMap(),
    val grades: Grades = Grades(),
    val teachingSubjects: Map<String, Boolean> = emptyMap(),
    @Deprecated("Use getSchedule() instead.", ReplaceWith("getSchedule().toStringMap()"))
    val timetable: Map<String, Boolean> = emptyMap()
): User(name, photo, gender, uid) {

    constructor(
        name: String = "",
        photo: String? = null,
        gender: Gender = Gender.NONE,
        uid: String = "",
        regions: List<String> = emptyList(),
        schools: List<String> = emptyList(),
        schoolCharacteristics: Map<String, Boolean> = emptyMap(),
        grades: Grades = Grades(),
        teachingSubjects: Map<String, Boolean> = emptyMap(),
        schedule: Timetable = emptyTimetable()
    ): this(name, photo, gender, uid, regions, schools, schoolCharacteristics, grades,
        teachingSubjects, schedule.toStringMap())

    @Suppress("DEPRECATION")
    @Exclude
    fun getSchedule(): Timetable = timetable.toTimetable()

    @Parcelize
    data class Grades(
        val tenth: Boolean = false,
        val eleventh: Boolean = false,
        val twelfth: Boolean = false
    ): Parcelable {

        constructor(vararg grades: Grade): this(
            tenth = Grade.TENTH in grades,
            eleventh = Grade.ELEVENTH in grades,
            twelfth = Grade.TWELFTH in grades
        )

        @Exclude
        fun toList(): List<Grade> {
            val list = mutableListOf<Grade>()

            if (tenth) list.add(Grade.TENTH)
            if (eleventh) list.add(Grade.ELEVENTH)
            if (twelfth) list.add(Grade.TWELFTH)

            return list
        }
    }
}
