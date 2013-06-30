package l.files.setting;

import static com.google.common.base.Preconditions.checkNotNull;
import android.content.SharedPreferences;

final class SortSetting implements Setting<SortBy> {

  private final SharedPreferences pref;

  public SortSetting(SharedPreferences pref) {
    this.pref = checkNotNull(pref, "pref");
  }

  @Override public SortBy get() {
    String value = pref.getString(key(), SortBy.NAME.name());
    try {
      return SortBy.valueOf(value);
    } catch (IllegalArgumentException e) {
      return SortBy.NAME;
    }
  }

  @Override public void set(SortBy sort) {
    pref.edit()
        .putString(key(), sort.name())
        .apply();
  }

  @Override public String key() {
    return "sort";
  }

}
