package l.files.ui;

import android.app.Fragment;
import android.os.Bundle;

public final class Fragments {

  public static <T extends Fragment> T setArgs(T instance, String argKey, String argVal) {
    Bundle args = new Bundle(1);
    args.putString(argKey, argVal);
    instance.setArguments(args);
    return instance;
  }

  public static <T extends Fragment> T setArgs(T instance, Bundle args) {
    instance.setArguments(args);
    return instance;
  }

  private Fragments() {}
}
