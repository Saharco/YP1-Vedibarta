package com.technion.vedibarta.database

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

object DatabaseVersioning {
    private val VERSION_TYPE = VersionType.development
    private const val VERSION_NUMBER = "1.0.2"

    val currentVersion = RegularVersion(VERSION_TYPE, VERSION_NUMBER)
    fun getTestVersion(name: String) = TestVersion(name)
}

enum class VersionType {
    production,
    development
}

internal interface Version {
    val instance: DocumentReference
}

class TestVersion
internal constructor(val name: String): Version {
    override val instance: DocumentReference
        get() = FirebaseFirestore.getInstance().collection("Tests").document(name)
}

class RegularVersion
internal constructor(private val type: VersionType, val name: String): Version {
    override val instance: DocumentReference
        get() = FirebaseFirestore.getInstance().collection(type.toString()).document(name)
}
