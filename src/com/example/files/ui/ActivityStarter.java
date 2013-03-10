package com.example.files.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

public interface ActivityStarter {

  /**
   * @see Context#startActivity(Intent)
   * @throws ActivityNotFoundException
   */
  void startActivity(Context context, Intent intent);

}
