package com.rahulrav.glassnotify;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Simple View Holder for a @link{AppItem}
 */
public class AppItemViewHolder {

  /**
   * The textview that has the packageName.
   */
  public TextView packageName;

  /**
   * The checkbox that has the whitelist status.
   */
  public CheckBox isWhiteListed;

  /**
   * The imageView that shows the application icon.
   */
  public ImageView imageView;
}
