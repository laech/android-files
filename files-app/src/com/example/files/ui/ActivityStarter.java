package com.example.files.ui;

import android.content.Context;
import android.content.Intent;

public interface ActivityStarter {

  /**
   * @see android.content.Context#startActivity(android.content.Intent)
   * @throws android.content.ActivityNotFoundException
   */
  void startActivity(Context context, Intent intent);

}
