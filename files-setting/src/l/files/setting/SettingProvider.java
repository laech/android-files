package l.files.setting;

import android.content.SharedPreferences;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An object designed to provide setting and handle setting update requests via
 * an event bus.
 */
abstract class SettingProvider<T> implements OnSharedPreferenceChangeListener {

  protected final String key;
  protected Bus bus;
  protected SharedPreferences pref;

  protected SettingProvider(String key) {
    this.key = checkNotNull(key, "key");
  }

  /**
   * Gets the current value of the setting. This method needs to be annotated
   * with {@link Produce} on override.
   */
  public abstract T get();

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (this.key.equals(key)) {
      bus.post(get());
    }
  }

  /**
   * Registers this instance on the given bus and preference.
   */
  void register(Bus bus, SharedPreferences pref) {
    this.bus = bus;
    this.pref = pref;
    pref.registerOnSharedPreferenceChangeListener(this);
    bus.register(this);
  }
}
