package com.example.files.content;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

public class ActivityStarter {

  public static final ActivityStarter INSTANCE = new ActivityStarter();

  ActivityStarter() {
  }

  /**
   * @see Context#startActivity(Intent)
   * @throws ActivityNotFoundException
   */
  public void startActivity(Context context, Intent intent) {
    context.startActivity(intent);
  }

}
