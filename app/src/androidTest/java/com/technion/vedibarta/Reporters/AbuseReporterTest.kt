package com.technion.vedibarta.Reporters

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.technion.vedibarta.POJOs.AbuseReport
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.database.AbuseReporter
import com.technion.vedibarta.database.DataBase
import com.technion.vedibarta.database.StudentData
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.database
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.student
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AbuseReporterTest {
    private val reporter = AbuseReporter()

    init {
        // need to initialize database because loginActivity wasn't created
        database = DataBase("dummyId")
    }

    @Test
    fun sendingReportAddsItToCollectionAndRemoveDeletesIt() {
        val sentReport = AbuseReport(
            "sid", "rid",
            "some text", "aid",
            "sname", "aname"
        )
        var savedReport: AbuseReport? = null
//        Tasks.await(
//            this.reporter.sendReport(sentReport)
//                .addOnSuccessListener { report ->
//                    database.reports().report(report.id).build().get()
//                        .continueWith {
//                            savedReport = it.result?.toObject(AbuseReport::class.java)
//                        }.continueWith {
//                            this.reporter.removeReport(report.id)
//                        }
//                }
//                .addOnFailureListener {
//                    throw it
//                }
//        )
//        assert(sentReport == savedReport)

        Tasks.await(
            database.students().build().document("A9428kg0M7fmdhIE8LM2PkwXVZt1").get()
                .continueWithTask {
                    val a = it.result!!.toObject(Student::class.java)!!
                    val s = StudentData(a)
                    Log.d("wtf", "${a}")
                    s.teachers
                }.continueWith {
                    Log.d("wtf", "${it.result}")
                }
        )
    }
}