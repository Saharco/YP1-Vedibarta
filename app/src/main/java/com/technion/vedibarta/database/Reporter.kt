package com.technion.vedibarta.database

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.database

/**
 * base class for sending and deleting reports reports
 */
abstract class Reporter<T: Report>
{
    protected val TAG = "Reporter"
    // what collection of reports to access
    protected abstract val reports: ICollectionPath

    fun sendReport(report: T) = reports.build().add(report)

    fun removeReport(rid: String) = reports.report(rid).build().delete()
}

/**
 * handles the AbuseReports
 */
class AbuseReporter : Reporter<AbuseReport>()
{
    override val reports: ICollectionPath
        get() = database.reports()

    fun report(reporterId: String, reporterName: String, reporterPhoto: String?, reporterGender: Gender,
               abuserId: String, abuserName: String, abuserPhoto: String?, abuserGender: Gender,
               teacherId: String, report: String): Task<DocumentReference>
    {
        val a = AbuseReport(reporterId, reporterName, reporterPhoto, reporterGender,
                            abuserId, abuserName, abuserPhoto, abuserGender,
                            teacherId, report)
        return this.sendReport(a)
    }
}