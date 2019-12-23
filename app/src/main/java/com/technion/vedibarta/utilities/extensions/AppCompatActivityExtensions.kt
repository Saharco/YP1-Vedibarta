package com.technion.vedibarta.utilities.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle

fun AppCompatActivity.isInForeground(): Boolean =
    this.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)