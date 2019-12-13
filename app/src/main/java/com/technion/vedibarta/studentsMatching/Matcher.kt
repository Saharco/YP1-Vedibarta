package com.technion.vedibarta.studentsMatching

import com.google.android.gms.tasks.Task
import com.technion.vedibarta.POJOs.Student

/**
 * An interface for a matching algorithm between students.
 */
interface Matcher {
    /**
     * Run the matching algorithm asynchronously and return the matched students.
     * @return a [Set] of [Student] objects, the matched students.
     */
    fun match(): Task<Set<Student>>
}