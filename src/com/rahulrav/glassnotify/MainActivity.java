package com.rahulrav.glassnotify;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

  /**
   * Status
   */
  TextView status;

  /**
   * Account task
   */
  PrimaryAccountTask accountTask;

  /**
   * Test notifications
   */
  Button notificationTest;

  /**
   * Setp whitelist.
   */
  Button setupWhiteList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    status = (TextView) findViewById(R.id.status);
    // attach listeners
    notificationTest = (Button) findViewById(R.id.notification_test);
    notificationTest.setOnClickListener(new NotificationTestHandler());
    setupWhiteList = (Button) findViewById(R.id.whitelist_setup);
    setupWhiteList.setOnClickListener(new WhitelistSetupHandler());
  }

  @Override
  protected void onResume() {
    super.onResume();
    accountTask = new PrimaryAccountTask(this, status);
    accountTask.execute();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (accountTask != null && !(accountTask.getStatus() == AsyncTask.Status.FINISHED)) {
      accountTask.cancel(true);
      accountTask = null;
    }
  }

  static void appendText(final TextView view, final String text) {
    if (view != null && !TextUtils.isEmpty(text)) {
      final String existingText = String.valueOf(view.getText());
      final StringBuilder builder = new StringBuilder();
      if (!TextUtils.isEmpty(existingText) && !existingText.contains(text)) {
        builder.append(existingText).append("\r\n");
      }
      builder.append(text);
      view.setText(builder.toString());
    }
  }

  /**
   * Displays the primary google account being used.
   */
  public static class PrimaryAccountTask extends AsyncTask<Void, Void, String> {
    private final Context context;
    private final TextView status;

    public PrimaryAccountTask(final Context context, final TextView status) {
      this.context = context;
      this.status = status;
    }

    @Override
    protected String doInBackground(Void... contexts) {
      return NotifierService.getPrimaryGmailAccount(context);
    }

    @Override
    protected void onPostExecute(String emailAddress) {
      MainActivity.appendText(status, String.format("Using primary account (%s)", emailAddress));
    }
  }

  /**
   * Triggers a test notification.
   */
  public static class NotificationTestHandler implements View.OnClickListener {
    @Override
    public void onClick(View view) {
      final Context context = view.getContext();
      final Notification notification = new Notification.Builder(context)
          .setContentTitle("Test Glass Notification")
          .setContentText("Hello Glass Notifier !")
          .setSmallIcon(android.R.drawable.ic_dialog_alert)
          .build();

      final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      manager.notify(0, notification);
    }
  }

  /**
   * Kicks off whitelist setup.
   */
  public static class WhitelistSetupHandler implements View.OnClickListener {
    @Override
    public void onClick(View view) {
      final Context context = view.getContext();
      final Intent intent = new Intent();
      intent.setClass(context, WhitelistActivity.class);
      context.startActivity(intent);
    }
  }

}
