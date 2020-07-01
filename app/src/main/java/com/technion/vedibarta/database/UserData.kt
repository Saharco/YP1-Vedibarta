package com.technion.vedibarta.database

import com.google.android.gms.tasks.Task
import com.technion.vedibarta.POJOs.AbuseReport
import com.technion.vedibarta.POJOs.Class
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.POJOs.Teacher
import com.technion.vedibarta.matching.impl.DocumentsMatcherImpl
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.database
import com.technion.vedibarta.utilities.extensions.queryToListOf
import com.technion.vedibarta.utilities.extensions.resultToListOf

/**
 * a utility function for getting reports of a user
 */
private fun getReportsByField(field: String, value: Any): Task<List<AbuseReport>>
{
    return database.reports().build()
        .whereEqualTo(field, value).get()
        .continueWith { task ->
            task.queryToListOf<AbuseReport>()
        }
}

/**
 * interface for getting data that is relevant to every user has
 */
interface UserData
{
    val classes: Task<List<Class>>
    val reports: Task<List<AbuseReport>>
}

/**
 * class for getting data that is relevant to student
 */
class StudentData(private val s: Student) : UserData
{
    private val id = s.uid
    override val classes: Task<List<Class>>
        get()
        {
            return database.classes().build()
                .whereArrayContains("studentsIDs", id).get()
                .continueWith { task ->
                    task.queryToListOf<Class>()
                }
        }

    override val reports: Task<List<AbuseReport>>
        get() = getReportsByField("senderId", id)

    val teachers: Task<List<Teacher>>
        get()
        {
            return getTeacherIds()
                .continueWithTask { ids ->
                    DocumentsMatcherImpl(database.teachers().build())
                        .whereFieldEqualsToOneOf("uid", ids.result!!)
                        .match()
                }.continueWith {task ->
                    task.resultToListOf<Teacher>()
                }
        }

    private fun getTeacherIds(): Task<List<String>>
    {
        return classes.continueWith { task ->
            task.result!!.map { it.teacherID }
        }
    }
}

/**
 * class for getting data that is relevant to teacher
 */
class TeacherData(private val t: Teacher)
{
    private val id = t.uid

    val classes: Task<List<Class>>
        get()
        {
            return database.classes().build()
                .whereEqualTo("teacherIDs", id).get()
                .continueWith { task ->
                    task.queryToListOf<Class>()
                }
        }

    val receivedReports: Task<List<AbuseReport>>
        get() = getReportsByField("receiverId", id)
}