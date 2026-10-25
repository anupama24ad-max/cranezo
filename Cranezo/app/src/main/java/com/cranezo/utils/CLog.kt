package com.cranezo.utils

import android.util.Log

object CLog {
   private val isLoggingAvailable = true
   @JvmStatic fun d(TAG: String, msg: String) {
       if (isLoggingAvailable) {
           Log.d(TAG, msg)
       }
   }
   @JvmStatic fun e(TAG: String, msg: String) {
       if (isLoggingAvailable) {
           Log.e(TAG, msg)
       }
   }
   @JvmStatic fun i(TAG: String, msg: String) {
       if (isLoggingAvailable) {
           Log.i(TAG, msg)
       }
   }
   @JvmStatic fun w(TAG: String, msg: String) {
       if (isLoggingAvailable) {
           Log.w(TAG, msg)
       }
   }
}
