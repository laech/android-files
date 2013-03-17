package com.example.files.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

public class ActivityStarter {

  /**
   * @see Context#startActivity(Intent)
   * @throws ActivityNotFoundException
   */
  public void startActivity(Context context, Intent intent) {
    context.startActivity(intent);
  }

}
