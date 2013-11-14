package l.files.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public final class Fragments {

  public static <T extends Fragment> T setArgs(T instance, String argKey, String argVal) {
    Bundle args = new Bundle(1);
    args.putString(argKey, argVal);
    instance.setArguments(args);
    return instance;
  }

  private Fragments() {}
}