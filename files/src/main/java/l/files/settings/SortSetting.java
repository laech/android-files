package l.files.settings;

import android.content.Context;
import android.content.SharedPreferences;
import com.squareup.otto.Bus;
import l.files.R;

import java.io.File;
import java.util.List;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.google.common.base.Preconditions.checkNotNull;

public final class SortSetting implements OnSharedPreferenceChangeListener {

  private static final String KEY = "sort";

  public static interface Transformer {
    List<Object> transform(Context context, File... files);
  }

  public enum Sort implements Transformer {

    NAME(
        R.string.name,
        new SortByName()
    ),

    DATE_MODIFIED(
        R.string.date_modified,
        new SortByDateModified()
    ),

    SIZE(
        R.string.size,
        null
    );

    private final int labelResId;
    private final Transformer transformer;

    private Sort(int labelResId, Transformer transformer) {
      this.labelResId = labelResId;
      this.transformer = transformer;
    }

    public String label(Context context) {
      return context.getString(labelResId);
    }

    @Override public List<Object> transform(Context context, File... files) {
      return transformer.transform(context, files);
    }

  }

  public static SortSetting create(SharedPreferences preferences, Bus bus) {
    SortSetting setting = new SortSetting(preferences, bus);
    preferences.registerOnSharedPreferenceChangeListener(setting);
    return setting;
  }

  private final SharedPreferences preferences;
  private final Bus bus;

  private SortSetting(SharedPreferences preferences, Bus bus) {
    this.preferences = checkNotNull(preferences, "preferences");
    this.bus = checkNotNull(bus, "bus");
  }

  public Sort get() {
    String pref = preferences.getString(KEY, Sort.NAME.name());
    try {
      return Sort.valueOf(pref);
    } catch (IllegalArgumentException e) {
      return Sort.NAME;
    }
  }

  public void set(Sort sort) {
    preferences.edit()
        .putString(KEY, sort.name())
        .apply();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (KEY.equals(key)) bus.post(get());
  }
}
