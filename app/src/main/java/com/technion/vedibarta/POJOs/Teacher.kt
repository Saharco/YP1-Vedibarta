package com.technion.vedibarta.POJOs

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable


/***
 * teacher representation in FireBase
 * [schoolsCharacteristics] schools characteristics combined and not per school
 * [ageBrackets] what grades does and doesn't the teacher teaches
 * [timetable] should use TimetableBuilder to get an instance of it
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class Teacher(val name: String = "",
                   val schools: MutableList<String> = mutableListOf(),
                   val schoolsCharacteristics: MutableMap<String, Boolean> = HashMap(),
                   val ageBrackets: MutableMap<String, Boolean> = HashMap(),
                   val teachingSubjects: MutableMap<String, Boolean> = HashMap(),
                   val timetable: Map<String, Boolean> = emptyMap()): Serializable, Parcelable

{
    class TimetableBuilder
    {
        private val m = mutableMapOf<String, Boolean>()

        //adds the day and hour to the table
        fun mark(day: Int, hour: Int) = apply { m["$day$hour"] = true }

        //removes the day and hour from the table
        fun unmark(day: Int, hour: Int) = apply { m["$day$hour"] = false }

        fun build() = m.toMap()
    }
}
