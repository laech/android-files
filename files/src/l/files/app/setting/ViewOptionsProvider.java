package l.files.app.setting;

import android.content.SharedPreferences;
import com.google.common.base.Supplier;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.google.common.base.Preconditions.checkNotNull;

final class ViewOptionsProvider
    implements Supplier<ViewOptionsEvent>, OnSharedPreferenceChangeListener {

  private static final String KEY_HIDDEN_FILES = "show-hidden-files";
  private static final String KEY_SORT = "sort";

  static ViewOptionsProvider register(Bus bus, SharedPreferences pref) {
    ViewOptionsProvider handler = new ViewOptionsProvider(bus, pref);
    bus.register(handler);
    pref.registerOnSharedPreferenceChangeListener(handler);
    return handler;
  }

  private final Bus bus;
  private final SharedPreferences pref;

  private ViewOptionsProvider(Bus bus, SharedPreferences pref) {
    this.bus = checkNotNull(bus, "bus");
    this.pref = checkNotNull(pref, "pref");
  }

  @Subscribe public void handle(SortRequest request) {
    pref.edit()
        .putString(KEY_SORT, request.sort().name())
        .apply();
  }

  @Subscribe public void handle(ShowHiddenFilesRequest request) {
    pref.edit()
        .putBoolean(KEY_HIDDEN_FILES, request.show())
        .apply();
  }

  @Produce @Override public ViewOptionsEvent get() {
    return new ViewOptionsEvent(sort(), showHiddenFiles());
  }

  private boolean showHiddenFiles() {
    return pref.getBoolean(KEY_HIDDEN_FILES, false);
  }

  private Sort sort() {
    String value = pref.getString(KEY_SORT, Sort.NAME.name());
    try {
      return Sort.valueOf(value);
    } catch (IllegalArgumentException e) {
      return Sort.NAME;
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (KEY_HIDDEN_FILES.equals(key) || KEY_SORT.equals(key)) bus.post(get());
  }
}
