package l.files.app;

import static com.google.common.collect.Lists.newArrayListWithCapacity;

import android.os.Bundle;
import java.util.ArrayList;

public final class Bundles {

  public static int getInt(Bundle nullableBundle, String key, int defaultVal) {
    if (nullableBundle == null) {
      return defaultVal;
    }
    return nullableBundle.getInt(key, defaultVal);
  }

  public static ArrayList<String> getStringList(Bundle nullableBundle, String key) {
    if (nullableBundle != null) {
      ArrayList<String> strings = nullableBundle.getStringArrayList(key);
      if (strings != null) {
        return strings;
      }
    }
    return newArrayListWithCapacity(0);
  }

  private Bundles() {}
}
