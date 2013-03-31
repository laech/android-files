package com.example.files.widget;

import android.content.Context;
import android.widget.Toast;

public class Toaster {

  public static final Toaster INSTANCE = new Toaster();

  Toaster() {
  }

  /**
   * @param duration {@link Toast#LENGTH_SHORT} or {@link Toast#LENGTH_LONG}
   * @see Toast#makeText(Context, int, int)
   */
  public void toast(Context context, int resId, int duration) {
    Toast.makeText(context, resId, duration).show();
  }

}
