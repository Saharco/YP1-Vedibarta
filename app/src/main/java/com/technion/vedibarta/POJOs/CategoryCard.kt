package com.technion.vedibarta.POJOs

import java.io.Serializable

data class CategoryCard(val title: String, val values: Array<String>) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CategoryCard

        if (title != other.title) return false
        if (!values.contentEquals(other.values)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + values.contentHashCode()
        return result
    }
}