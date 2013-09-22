package l.files.app;

import static java.util.Collections.emptyList;

import android.os.Bundle;
import java.util.List;

public final class Bundles {

  public static int getInt(Bundle bundle, String key, int defaultVal) {
    if (bundle == null) {
      return defaultVal;
    }
    return bundle.getInt(key, defaultVal);
  }

  public static List<String> getStringList(Bundle bundle, String key) {
    if (bundle == null) {
      return emptyList();
    }
    List<String> strings = bundle.getStringArrayList(key);
    if (strings != null) {
      return strings;
    }
    return emptyList();
  }

  private Bundles() {}
}
