package com.example.files.ui;

import android.content.Context;
import android.widget.Toast;

public interface Toaster {

  /**
   * @param duration {@link Toast#LENGTH_SHORT} or {@link Toast#LENGTH_LONG}
   * @see Toast#makeText(Context, int, int)
   */
  void toast(Context context, int resId, int duration);
}
