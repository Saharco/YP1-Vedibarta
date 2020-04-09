package com.technion.vedibarta.utilities.extensions

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.storage.CancellableTask
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
