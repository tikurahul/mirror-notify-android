package com.rahulrav.glassnotify;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class WhitelistActivity extends Activity {

  private ProgressDialog progressDialog;

  private ListView listView;

  private AppItemAdapter adapter;

  private LoadApplicationsTask loadApplicationsTask;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.whitelist);
    final SharedPreferences preferences = this.getSharedPreferences(NotifierService.PREFERENCES_NAME, MODE_PRIVATE);
    adapter = new AppItemAdapter(this, null);
    listView = (ListView) findViewById(R.id.list_view);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(new AppItemAdapter.WhiteListListener(preferences));
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (loadApplicationsTask != null && !(loadApplicationsTask.getStatus() == AsyncTask.Status.FINISHED)) {
      loadApplicationsTask.cancel(true);
      loadApplicationsTask = null;
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    progressDialog = ProgressDialog.show(this, "Glass Notifier", "Loading the list of applications on your device", true);
    loadApplicationsTask = new LoadApplicationsTask(this);
    loadApplicationsTask.execute();
  }

  /**
   * Displays the primary google account being used.
   */
  public static class LoadApplicationsTask extends AsyncTask<Void, Void, List<AppItem>> {

    private final WhitelistActivity activity;

    public LoadApplicationsTask(final WhitelistActivity activity) {
      this.activity = activity;
    }

    @Override
    protected List<AppItem> doInBackground(Void... nothing) {
      final SharedPreferences preferences = activity.getSharedPreferences(NotifierService.PREFERENCES_NAME, MODE_PRIVATE);
      final PackageManager packageManager = activity.getPackageManager();
      final List<ApplicationInfo> applicationInfos = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
      List<AppItem> appItems = new LinkedList<AppItem>();
      if (applicationInfos != null) {
        for (final ApplicationInfo applicationInfo : applicationInfos) {
          final AppItem appItem = new AppItem();
          appItem.packageName = applicationInfo.packageName;
          final Drawable drawable = applicationInfo.loadIcon(packageManager);
          appItem.drawable = drawable;
          appItem.applicationLabel = String.valueOf(packageManager.getApplicationLabel(applicationInfo));
          appItem.isWhitelisted = NotifierService.getWhiteListStatus(preferences, activity.getPackageName(), applicationInfo.packageName);
          appItems.add(appItem);
        }
      }
      // sort
      Collections.sort(appItems, new Comparator<AppItem>() {
        @Override
        public int compare(final AppItem first, final AppItem second) {
          final String labelOne = TextUtils.isEmpty(first.applicationLabel) ? first.packageName : first.applicationLabel;
          final String labelTwo = TextUtils.isEmpty(second.applicationLabel) ? second.packageName : second.applicationLabel;
          return labelOne.compareTo(labelTwo);
        }
      });
      return appItems;
    }

    @Override
    protected void onPostExecute(final List<AppItem> items) {
      activity.adapter.clear();
      activity.adapter.addItems(items);
      activity.adapter.notifyDataSetChanged();
      activity.progressDialog.dismiss();
    }
  }
}