package l.files.event;

import android.content.SharedPreferences;
import com.squareup.otto.Bus;
import junit.framework.TestCase;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public final class SettingProviderTest extends TestCase {

  private String key;
  private String value;
  private Bus bus;
  private SharedPreferences pref;

  private SettingProvider<String> provider;

  @Override protected void setUp() throws Exception {
    super.setUp();
    key = "my-key";
    value = "hello";
    provider = new SettingProvider<String>(key) {
      @Override public String get() {
        return value;
      }
    };

    bus = mock(Bus.class);
    pref = mock(SharedPreferences.class);
    provider.register(bus, pref);
  }

  public void testOnSharedPreferenceChanged_postEventIfKeyIsSame() {
    provider.onSharedPreferenceChanged(null, key);
    verify(bus).post(value);
  }

  public void testOnSharedPreferenceChanged_doesNothingIfKeyIsDifferent() {
    provider.onSharedPreferenceChanged(null, key + "blah");
    verify(bus, never()).post(any());
  }

  public void testRegister() {
    verify(bus).register(provider);
    verify(pref).registerOnSharedPreferenceChangeListener(provider);
  }
}
