package com.rahulrav.glassnotify.util;

import android.util.Log;
import com.rahulrav.glassnotify.BuildConfig;

/**
 * The application Logger.
 */
public final class Logger {

  private static final String TAG = "GlassNotifier";

  public static void d(final String message) {
    if (BuildConfig.DEBUG) {
      Log.d(TAG, message);
    }
  }

  public static void d(final String message, final Throwable throwable) {
    if (BuildConfig.DEBUG) {
      Log.d(TAG, message, throwable);
    }
  }

  public static void i(final String message) {
    if (BuildConfig.DEBUG) {
      Log.i(TAG, message);
    }
  }

  public static void i(final String message, final Throwable throwable) {
    if (BuildConfig.DEBUG) {
      Log.i(TAG, message, throwable);
    }
  }

  public static void e(final String message) {
    Log.e(TAG, message);
  }

  public static void e(final String message, final Throwable throwable) {
    Log.e(TAG, message, throwable);
  }

}
