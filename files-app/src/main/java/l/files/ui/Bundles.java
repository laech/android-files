package l.files.ui;

import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;

public final class Bundles {

  public static int getInt(Bundle nullableBundle, String key, int defaultVal) {
    if (nullableBundle == null) {
      return defaultVal;
    }
    return nullableBundle.getInt(key, defaultVal);
  }

  public static <T extends Parcelable> ArrayList<T> getParcelableArrayList(Bundle nullableBundle, String key, Class<T> clazz) {
    if (nullableBundle == null) {
      return new ArrayList<>(0);
    }

    ArrayList<T> items = nullableBundle.getParcelableArrayList(key);
    if (items == null) {
      return new ArrayList<>(0);
    }

    for (T item : items) {
      if (!(clazz.isInstance(item))) {
        return new ArrayList<>(0);
      }
    }

    return items;
  }

  private Bundles() {}
}
