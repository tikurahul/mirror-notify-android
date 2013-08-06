package com.rahulrav.glassnotify.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class IOUtils {

  /**
   * Reads a @link{InputStream} till the end of the stream is reached.
   *
   * @param in      is the @link{InputStream}
   * @param closeIn indicates to close the stream after the end of stream has been reached
   * @return the @link{String} contents of the stream
   */
  public static String readAsString(final InputStream in, final boolean closeIn) {
    if (in == null) {
      return null;
    }

    ByteArrayOutputStream out = null;
    try {
      out = new ByteArrayOutputStream();
      final byte[] buffer = new byte[4096];
      int length = -1;
      while ((length = in.read(buffer)) >= 0) {
        out.write(buffer, 0, length);
      }
      return new String(out.toByteArray(), "UTF-8");
    } catch (final Exception e) {
      Logger.e("Error reading input stream", e);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (Exception ignore) {
          Logger.d("Error closing stream", ignore);
        }
      }
      if (in != null && closeIn) {
        try {
          in.close();
        } catch (Exception ignore) {
          Logger.d("Error closing stream", ignore);
        }
      }
    }
    return null;
  }
}
