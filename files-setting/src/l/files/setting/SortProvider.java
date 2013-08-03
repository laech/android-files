package l.files.setting;

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

final class SortProvider extends SettingProvider<SortSetting> {

  private final String defaultSort;

  SortProvider(String defaultSort) {
    super("sort");
    this.defaultSort = checkNotNull(defaultSort, "defaultSort");
  }

  @Produce @Override public SortSetting get() {
    return new SortSetting(pref.getString(key, defaultSort));
  }

  @Subscribe public void set(SortRequest request) {
    pref.edit()
        .putString(key, request.value())
        .apply();
  }
}
