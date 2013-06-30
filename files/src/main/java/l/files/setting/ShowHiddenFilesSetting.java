package l.files.setting;

import static com.google.common.base.Preconditions.checkNotNull;
import android.content.SharedPreferences;

final class ShowHiddenFilesSetting implements Setting<Boolean> {

  private final SharedPreferences pref;

  public ShowHiddenFilesSetting(SharedPreferences pref) {
    this.pref = checkNotNull(pref, "pref");
  }

  @Override public Boolean get() {
    return pref.getBoolean(key(), false);
  }

  @Override public void set(Boolean value) {
    checkNotNull(value, "value");
    pref.edit()
        .putBoolean(key(), value)
        .apply();
  }

  @Override public String key() {
    return "show-hidden-files";
  }

}
