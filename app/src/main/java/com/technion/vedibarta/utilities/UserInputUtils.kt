package com.technion.vedibarta.utilities

import android.util.Patterns


fun CharSequence.isEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches()