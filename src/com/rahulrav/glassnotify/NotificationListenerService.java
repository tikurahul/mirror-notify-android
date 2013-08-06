package com.rahulrav.glassnotify;

import android.R;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.rahulrav.glassnotify.util.Logger;

import java.io.ByteArrayOutputStream;

/**
 * The *only* way to intercept Android Notifications globally, is to use a
 * {@link AccessibilityService}. This service only listens for notifications. <br/>
 * <p/>
 * It sends intents to an {@link IntentService} to forward notifications to
 * Glass.
 *
 * @author rahulrav
 */
public class NotificationListenerService extends AccessibilityService {

  /**
   * Represents the notification action for the @link{IntentService}.
   */
  public static final String GLASS_NOTIFY = "com.rahulrav.glassnotify.action.NOTIFY";

  /**
   * Other interesting constants.
   */
  public static final String PACKAGE_NAME = "packageName";
  public static final String TEXT = "text";

  private static final String DELIMITER = "#";

  boolean initCompleted = false;

  @Override
  public void onAccessibilityEvent(final AccessibilityEvent event) {
    if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
      final String packageName = String.valueOf(event.getPackageName());

      // check whitelist
      final SharedPreferences preferences = getSharedPreferences(NotifierService.PREFERENCES_NAME, MODE_PRIVATE);
      final boolean isWhiteListed = NotifierService.getWhiteListStatus(preferences, getPackageName(), packageName);
      if (!isWhiteListed) {
        Logger.i(String.format("Package (%s) is not in the whitelist, ignoring.", packageName));
        return;
      }

      // represents the actual notification
      final Parcelable payload = event.getParcelableData();
      ByteArrayOutputStream out = null;
      try {
        // check for a notification
        if (!(payload instanceof Notification)) {
          return;
        }

        final Notification notification = (Notification) payload;
        final RemoteViews contentView = notification.contentView;

        final LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(contentView.getLayoutId(), null);
        contentView.reapply(getApplicationContext(), viewGroup);

        String title = null;
        final TextView titleView = (TextView) viewGroup.findViewById(R.id.title);
        if (titleView != null) {
          title = String.valueOf(titleView.getText());
        }

        final String content = collectTextViews(viewGroup, null).toString();
        final String message = formatMessage(title, content);

        // create the intent
        final Intent intent = new Intent(GLASS_NOTIFY);
        intent.setClass(getApplicationContext(), NotifierService.class);
        final Bundle bundle = new Bundle();
        bundle.putString(PACKAGE_NAME, packageName);
        if (title != null) {
          bundle.putString(TEXT, message);
        }
        intent.replaceExtras(bundle);
        getApplicationContext().startService(intent);
      } finally {
        if (out != null) {
          try {
            out.close();
          } catch (final Exception ignore) {
            // ignore
          }
        }
      }
    }
  }

  /**
   * Formats the message.
   */
  private String formatMessage(final String title, final String content) {
    final StringBuilder message = new StringBuilder();
    if (!TextUtils.isEmpty(title)) {
      message.append(title).append("\r\n");
    }
    if (!TextUtils.isEmpty(content)) {
      final String[] splitContents = content.split(DELIMITER);
      if (splitContents != null) {
        for (int i = 0; i < splitContents.length; i++) {
          if (!TextUtils.isEmpty(splitContents[i])) {
            message.append(splitContents[i].trim());
            if (i != splitContents.length - 1) {
              message.append(", ");
            }
          }
        }
      }
    }
    return message.toString();
  }

  /**
   * Collects all the TextViews other than the title.
   */
  private StringBuilder collectTextViews(final View view, StringBuilder builder) {

    if (builder == null) {
      builder = new StringBuilder();
    }

    if (view == null) {
      return builder;
    }

    if (view instanceof TextView) {
      final TextView textView = (TextView) view;
      // if its not a title, and you have not already collected it - collect it
      if (textView.getId() != R.id.title) {
        final String text = String.valueOf(textView.getText());
        if (builder.indexOf(text) < 0) {
          return builder.append(textView.getText()).append(DELIMITER);
        } else {
          return builder;
        }
      }
    }

    if (view instanceof ViewGroup) {
      final ViewGroup viewGroup = (ViewGroup) view;
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        collectTextViews(viewGroup.getChildAt(i), builder);
      }
    }

    return builder;
  }

  @Override
  protected void onServiceConnected() {
    if (initCompleted) {
      return;
    }

    final AccessibilityServiceInfo info = new AccessibilityServiceInfo();
    info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
    setServiceInfo(info);
    initCompleted = true;
  }

  @Override
  public void onInterrupt() {
    initCompleted = false;
  }

}
