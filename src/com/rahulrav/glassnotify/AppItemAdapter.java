package com.rahulrav.glassnotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.rahulrav.glassnotify.util.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class AppItemAdapter extends BaseAdapter {

  private final Context context;
  private final LayoutInflater layoutInflator;
  private final SharedPreferences preferences;
  protected final List<AppItem> items;

  public AppItemAdapter(final Context context, final List<AppItem> items) {
    this.context = context;
    layoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    preferences = context.getSharedPreferences(NotifierService.PREFERENCES_NAME, Context.MODE_PRIVATE);
    this.items = new LinkedList<AppItem>();
    if (items != null && !items.isEmpty()) {
      this.items.addAll(items);
    }
  }

  @Override
  public int getCount() {
    return items.size();
  }

  @Override
  public Object getItem(final int position) {
    return items.get(position);
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public long getItemId(final int position) {
    return position;
  }

  public void addItems(final Collection<AppItem> items) {
    this.items.addAll(items);
  }

  public void clear() {
    items.clear();
  }

  @Override
  public View getView(final int position, View convertView, final ViewGroup parent) {
    AppItemViewHolder viewHolder;
    AppItem item = (AppItem) getItem(position);
    if (convertView == null) {
      convertView = layoutInflator.inflate(R.layout.whitelist_item, parent, false);
      viewHolder = new AppItemViewHolder();
      viewHolder.packageName = (TextView) convertView.findViewById(R.id.package_name);
      viewHolder.isWhiteListed = (CheckBox) convertView.findViewById(R.id.is_whitelisted);
      viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (AppItemViewHolder) convertView.getTag();
    }
    if (TextUtils.isEmpty(item.applicationLabel)) {
      viewHolder.packageName.setText(item.packageName);
    } else {
      viewHolder.packageName.setText(item.applicationLabel);
    }
    final boolean isWhitelisted = NotifierService.getWhiteListStatus(preferences, context.getPackageName(), item.packageName);
    viewHolder.isWhiteListed.setChecked(isWhitelisted);
    viewHolder.imageView.setImageDrawable(item.drawable);
    return convertView;
  }

  public static class WhiteListListener implements AdapterView.OnItemClickListener {

    private final SharedPreferences preferences;

    public WhiteListListener(final SharedPreferences preferences) {
      this.preferences = preferences;
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
      try {
        final AppItem item = (AppItem) adapterView.getItemAtPosition(position);
        final String whiteList = preferences.getString(NotifierService.WHITE_LIST, null);
        // invert whitelist status
        item.isWhitelisted = !item.isWhitelisted;
        // update view
        final AppItemViewHolder viewHolder = (AppItemViewHolder) view.getTag();
        viewHolder.isWhiteListed.setChecked(item.isWhitelisted);
        // update
        JSONObject whiteListItems;
        if (TextUtils.isEmpty(whiteList)) {
          whiteListItems = new JSONObject();
        } else {
          whiteListItems = new JSONObject(whiteList);
        }
        whiteListItems.put(item.packageName, item.toJSON());
        // save
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(NotifierService.WHITE_LIST, whiteListItems.toString());
        editor.apply();
      } catch (final JSONException e) {
        Logger.e("Unable to update whitelist", e);
      }
    }
  }
}
