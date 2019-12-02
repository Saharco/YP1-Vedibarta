package com.technion.vedibarta.POJOs

import com.technion.vedibarta.utilities.Gender
import java.io.Serializable
import java.sql.Timestamp

data class Student(
    var name: String,
    var photo: String?,
    var region: String,
    var school: String,
    var gender: Gender,
    var lastActivity: Timestamp,
    var characteristics: List<String>,
    var hobbies: List<String>) : Serializable {
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
        if (characteristics != other.characteristics) return false
        if (hobbies != other.hobbies) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + photo.hashCode()
        result = 31 * result + region.hashCode()
        result = 31 * result + school.hashCode()
        result = 31 * result + gender.hashCode()
        result = 31 * result + lastActivity.hashCode()
        result = 31 * result + characteristics.hashCode()
        result = 31 * result + hobbies.hashCode()
        return result
    }

}