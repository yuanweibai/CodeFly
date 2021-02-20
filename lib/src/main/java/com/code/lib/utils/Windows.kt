package com.code.lib.utils

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import java.lang.ref.WeakReference

object Windows {

    fun hideSoftKeyboard(activity: Activity) {
        try {
            val activityWeakReference = WeakReference(activity)
            val inputMethodManager = activityWeakReference.get()!!
                .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                activityWeakReference.get()!!.currentFocus!!.windowToken, 0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}