package com.technion.vedibarta.utilities.extensions

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.CancellableTask
import com.technion.vedibarta.POJOs.Error
import com.technion.vedibarta.POJOs.LoadableData
import com.technion.vedibarta.POJOs.Loaded
import com.technion.vedibarta.POJOs.SlowLoadingEvent
import java.lang.Exception
import java.util.*

fun <TResult> Task<TResult>.afterTimeoutInMillis(millis: Long, timeoutResult: TResult): Task<TResult> {
    val taskCompletionSource = TaskCompletionSource<TResult>()

    fun completeTask(result: () -> TResult) = try {
        taskCompletionSource.trySetResult(result())
    } catch (e: Exception) {
        taskCompletionSource.trySetException(e)
    }

    val timer = Timer().apply {
        schedule(object : TimerTask() {
            override fun run() {
                completeTask { timeoutResult }
            }
        }, millis)
    }

    this.addOnCompleteListener { task ->
        timer.cancel()
        if (task.isSuccessful) {
            completeTask { task.result!! }
        } else {
            completeTask { timeoutResult }
        }
    }

    return taskCompletionSource.task
}

fun <TResult> CancellableTask<TResult>.cancelAfterTimeoutInMillis(millis: Long): Task<TResult> {
    val timer = Timer().apply {
        schedule(object : TimerTask() {
            override fun run() {
                this@cancelAfterTimeoutInMillis.cancel()
            }
        }, millis)
    }

    return addOnSuccessListener { timer.cancel() }
}

fun <TResult> Task<TResult>.executeAfterTimeoutInMillis(millis: Long=5000L, function: () -> Unit): Task<TResult> {
    val timer = Timer().apply {
        schedule(object : TimerTask() {
            override fun run() {
                if (!this@executeAfterTimeoutInMillis.isSuccessful)
                    function()
            }
        }, millis)
    }
    return addOnSuccessListener { timer.cancel() }
}

fun <T> Task<*>.handleError(data: MutableLiveData<LoadableData<T>>) =
    this.addOnFailureListener {
        data.value = Error(it.message)
    }

fun <T> Task<out T>.handleSuccess(data: MutableLiveData<LoadableData<T>>) =
    this.addOnSuccessListener {
        data.value = Loaded<T>(it)
    }

fun <T> Task<*>.handleTimeout(data: MutableLiveData<LoadableData<T>>) =
    this.executeAfterTimeoutInMillis {
        data.postValue(SlowLoadingEvent())
    }

inline fun <reified S> Task<List<DocumentSnapshot>>.resultToListOf() =
    this.result?.map { it.toObject(S::class.java)!! } ?: emptyList()

inline fun <reified S> Task<QuerySnapshot>.queryToListOf() =
    this.result?.map { it.toObject(S::class.java)!! } ?: emptyList()