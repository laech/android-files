package com.example.files.ui;

import android.content.Context;
import android.widget.Toast;

public interface Toaster {

  /**
   * @param duration {@link Toast#LENGTH_SHORT} or {@link Toast#LENGTH_LONG}
   * @see android.widget.Toast#makeText(android.content.Context, int, int)
   */
  void toast(Context context, int resId, int duration);
}
