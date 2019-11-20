package com.technion.vedibarta.POJOs

import com.technion.vedibarta.utilities.Gender
import java.io.Serializable
import java.sql.Timestamp

data class Student(
    val name: String,
    val photo: String?,
    val region: String,
    val school: String,
    val gender: Gender,
    val lastActivity: Timestamp,
    val characteristics: Array<String>,
    val hobbies: Array<String>) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Student

        if (name != other.name) return false
        if (photo != other.photo) return false
        if (region != other.region) return false
        if (school != other.school) return false
        if (gender != other.gender) return false
        if (lastActivity != other.lastActivity) return false
        if (!characteristics.contentEquals(other.characteristics)) return false
        if (!hobbies.contentEquals(other.hobbies)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + photo.hashCode()
        result = 31 * result + region.hashCode()
        result = 31 * result + school.hashCode()
        result = 31 * result + gender.hashCode()
        result = 31 * result + lastActivity.hashCode()
        result = 31 * result + characteristics.contentHashCode()
        result = 31 * result + hobbies.contentHashCode()
        return result
    }

}