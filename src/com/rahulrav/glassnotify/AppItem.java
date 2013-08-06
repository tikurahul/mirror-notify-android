package com.rahulrav.glassnotify;

import android.graphics.drawable.Drawable;
import com.rahulrav.glassnotify.util.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class AppItem {

  public String packageName;
  public String applicationLabel;
  public Drawable drawable;
  public boolean isWhitelisted;

  public static AppItem fromJSON(final JSONObject jobj) {
    if (jobj == null) {
      return null;
    }
    final AppItem item = new AppItem();
    item.packageName = jobj.optString("packageName", null);
    item.isWhitelisted = jobj.optBoolean("isWhiteListed", false);
    return item;
  }

  public JSONObject toJSON() {
    try {
      final JSONObject jobj = new JSONObject();
      jobj.put("packageName", this.packageName);
      jobj.put("isWhiteListed", this.isWhitelisted);
      return jobj;
    } catch (JSONException e) {
      Logger.e("Error creating JSONObject", e);
    }
    return null;
  }
}
