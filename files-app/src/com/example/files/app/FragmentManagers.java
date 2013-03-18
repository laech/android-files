package com.example.files.app;

import android.app.FragmentManager;

public class FragmentManagers {

  public static void popAllBackStacks(FragmentManager fm) {
    int count = fm.getBackStackEntryCount();
    while (count-- > 0)
      fm.popBackStack();
  }

  private FragmentManagers() {
  }
}
