package com.technion.vedibarta.POJOs

import java.io.Serializable

data class HobbyCard(val title: String, val hobbies: Array<String>) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HobbyCard

        if (title != other.title) return false
        if (!hobbies.contentEquals(other.hobbies)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + hobbies.contentHashCode()
        return result
    }
}