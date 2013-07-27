package l.files;

import android.content.SharedPreferences;
import com.google.common.base.Supplier;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import l.files.event.SortRequest;
import l.files.event.ViewEvent;
import l.files.setting.SortBy;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.google.common.base.Preconditions.checkNotNull;

final class ViewHandler
    implements Supplier<ViewEvent>, OnSharedPreferenceChangeListener {

  private static final String KEY_HIDDEN_FILES = "show-hidden-files";
  private static final String KEY_SORT = "sort";

  static ViewHandler register(Bus bus, SharedPreferences pref) {
    ViewHandler handler = new ViewHandler(bus, pref);
    bus.register(handler);
    pref.registerOnSharedPreferenceChangeListener(handler);
    return handler;
  }

  private final Bus bus;
  private final SharedPreferences pref;

  private ViewHandler(Bus bus, SharedPreferences pref) {
    this.bus = checkNotNull(bus, "bus");
    this.pref = checkNotNull(pref, "pref");
  }

  @Subscribe public void handle(SortRequest request) {
    pref.edit()
        .putString(KEY_SORT, request.sort().name())
        .apply();
  }

  @Produce @Override public ViewEvent get() {
    return new ViewEvent(sort(), showHiddenFiles());
  }

  private boolean showHiddenFiles() {
    return pref.getBoolean(KEY_HIDDEN_FILES, false);
  }

  private SortBy sort() {
    String value = pref.getString(KEY_SORT, SortBy.NAME.name());
    try {
      return SortBy.valueOf(value);
    } catch (IllegalArgumentException e) {
      return SortBy.NAME;
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (KEY_HIDDEN_FILES.equals(key) || KEY_SORT.equals(key)) bus.post(get());
  }

}
