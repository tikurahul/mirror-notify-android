package com.rahulrav.glassnotify;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;
import com.rahulrav.glassnotify.util.IOUtils;
import com.rahulrav.glassnotify.util.Logger;
import com.squareup.okhttp.OkHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Pattern;

/**
 * The {@link IntentService} that does the notifies http://glass-notify.appspot.com
 */
public class NotifierService extends IntentService {

  /** The name of the SharedPreference file. */
  public static final String PREFERENCES_NAME = "glass.notifications";

  /** The primary google account preference. */
  public static final String GOOGLE_ACCOUNT_PREF = "primary.google.account";

  /** A list of applications that have been white listed. */
  public static final String WHITE_LIST = "whitelisted.apps";

  /** The endpoint. */
  private static final String ENDPOINT = "http://glass-notify.appspot.com/addToTimeLine";

  /**
   * The name of the worker thread.
   */
  private static final String WORKER_NAME = "Notifier";

  public NotifierService() {
    super(WORKER_NAME);
  }

  @Override
  protected void onHandleIntent(final Intent intent) {

    final String action = intent.getAction();

    if (TextUtils.isEmpty(action)) {
      return;
    }

    final String  primaryEmailAddress = getPrimaryGmailAccount(getApplicationContext());

    if (TextUtils.isEmpty(primaryEmailAddress)) {
      Logger.i(String.format("Ignoring action (%s) - no gmail account.", NotificationListenerService.GLASS_NOTIFY));
      Toast.makeText(getApplicationContext(), "Cannot find a primary google account to use Google Glass Notifier.", Toast.LENGTH_LONG).show();
      return;
    }

    if (action.equals(NotificationListenerService.GLASS_NOTIFY)) {
      final Bundle extras = intent.getExtras();
      if (extras == null) {
        Logger.i(String.format("Ignoring action (%s) - no extras.", NotificationListenerService.GLASS_NOTIFY));
        return;
      }

      final String packageName = extras.getString(NotificationListenerService.PACKAGE_NAME);
      final String message = extras.getString(NotificationListenerService.TEXT);

      if (TextUtils.isEmpty(packageName)) {
        Logger.i(String.format("Ignoring action (%s) - no packageName.", NotificationListenerService.GLASS_NOTIFY));
        return;
      }

      if (TextUtils.isEmpty(message)) {
        Logger.i(String.format("Ignoring action (%s) - no message.", NotificationListenerService.GLASS_NOTIFY));
        return;
      }

      try {
        final String notification = String.format("<article>" +
                                                     "<section>" +
                                                        "<div class='text-auto-size'>" +
                                                          "<p class='yellow'>%s</p>" +
                                                          "<p>%s</p>" +
                                                        "</div>" +
                                                     "</section>" +
                                                     "<footer>" +
                                                      "<div class='text-auto-size'>Glass Notifier</div>" +
                                                    "</footer>" +
                                                  "</article>", packageName, message);

        Logger.i(String.format("Notification (%s)", notification));
        // blacklist here -- new feature
        notify(primaryEmailAddress, notification, null, "True");
      } catch (final Exception e) {
        Logger.e("Unable to notify.", e);
      }
    }
  }

  private void notify(final String email, final String message, final String image, final String isHtml) throws Exception {
    final StringBuilder query = new StringBuilder();
    query.append("user_email=").append(URLEncoder.encode(email, "UTF-8")).append("&");
    query.append("is_html=").append(URLEncoder.encode(isHtml, "UTF-8")).append("&");
    query.append("f=").append(URLEncoder.encode("json", "UTF-8")).append("&");
    query.append("message=").append(URLEncoder.encode(message, "UTF-8"));
    if (!TextUtils.isEmpty(image)) {
      query.append("&").append("image=").append(URLEncoder.encode(image, "UTF-8"));
    }
    final OkHttpClient client = new OkHttpClient();
    final HttpURLConnection connection = client.open(new URL(ENDPOINT));
    OutputStream out = null;
    InputStream in = null;
    try {
      connection.setRequestMethod("POST");
      out = connection.getOutputStream();
      out.write(query.toString().getBytes("UTF-8"));
      out.close();

      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        Logger.e(String.format("Unexpected HTTP Response code %s", connection.getResponseCode()));
        return;
      }

      final String response = IOUtils.readAsString(connection.getInputStream(), false);
      final boolean success = !response.contains("error");
      if (success) {
        Logger.i("Notification complete.");
      } else {
        Logger.e(String.format("Unable to notify (%s)", response));
      }
    } finally {
      if (out != null) {
        out.close();
      }
      if (in != null) {
        in.close();
      }
    }
  }

  public static String getPrimaryGmailAccount(final Context context) {
    if (context == null) {
      return null;
    }
    final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
    String primaryEmailAddress = preferences.getString(GOOGLE_ACCOUNT_PREF, null);
    if (!TextUtils.isEmpty(primaryEmailAddress)) {
      return primaryEmailAddress;
    }

    // figure out the primary gmail address
    final Pattern emailPattern = Patterns.EMAIL_ADDRESS;
    final Account[] accounts = AccountManager.get(context).getAccounts();
    if (accounts != null) {
      for (final Account account : accounts) {
        if (emailPattern.matcher(account.name).matches() && account.name.contains("gmail")) {
          final SharedPreferences.Editor editor = preferences.edit();
          editor.putString(GOOGLE_ACCOUNT_PREF, account.name).apply();
          return account.name;
        }
      }
    }

    return null;
  }

  public static boolean getWhiteListStatus(final SharedPreferences preferences, final String currentAppPackageName, final String packageName) {
    try {
      final String whiteList = preferences.getString(NotifierService.WHITE_LIST, null);
      if (TextUtils.isEmpty(whiteList)) {
        return (packageName.equals(currentAppPackageName));
      }
      final JSONObject jobj = new JSONObject(whiteList);
      final JSONObject itemJson = jobj.optJSONObject(packageName);
      if (itemJson == null) {
        return (packageName.equals(currentAppPackageName));
      } else {
        final AppItem item = AppItem.fromJSON(itemJson);
        return item.isWhitelisted;
      }
    } catch (final JSONException e) {
      return false;
    }
  }
}