package l.files.ui.app.sidebar;

import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import com.squareup.otto.Bus;
import l.files.setting.SetSetting;
import l.files.test.TestSidebarFragmentActivity;
import l.files.ui.event.FileSelectedEvent;

import java.io.File;

import static java.util.Collections.singleton;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public final class SidebarFragmentTest
    extends ActivityInstrumentationTestCase2<TestSidebarFragmentActivity> {

  public SidebarFragmentTest() {
    super(TestSidebarFragmentActivity.class);
  }

  public void testBusIsNotifiedOnFileSelection() throws Throwable {
    final File file = new File("/");
    Bus bus = fragment().bus = mock(Bus.class);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        adapter().clear();
        adapter().add(file);
        listView().performItemClick(null, 0, 0);
      }
    });

    verify(bus).post(new FileSelectedEvent(file));
  }

  public void testBusIsNotNotifiedOnNonFileSelection() throws Throwable {
    Bus bus = fragment().bus = mock(Bus.class);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        adapter().clear();
        adapter().add("hello");
        listView().performItemClick(null, 0, 0);
      }
    });

    verifyZeroInteractions(bus);
  }

  public void testPreferenceListenerIsRegisteredOnResume() throws Throwable {
    fragment().pref = mock(SharedPreferences.class);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        fragment().onResume();
      }
    });

    verify(fragment().pref).registerOnSharedPreferenceChangeListener(fragment());
  }

  public void testPreferenceListenerIsUnregisteredOnPause() throws Throwable {
    fragment().pref = mock(SharedPreferences.class);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        fragment().onPause();
      }
    });

    verify(fragment().pref).unregisterOnSharedPreferenceChangeListener(fragment());
  }

  @SuppressWarnings("unchecked")
  public void testRefreshesOnFavoritesChange() throws Throwable {
    File file = new File("/");
    SetSetting<File> setting = fragment().setting = mock(SetSetting.class);
    given(setting.key()).willReturn("test-key");
    given(setting.get()).willReturn(singleton(file));

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        fragment().onSharedPreferenceChanged(null, fragment().setting.key());
      }
    });

    assertAdapterContains(file);
  }

  @SuppressWarnings("unchecked")
  public void testIncludesFavoriteInSidebar() throws Throwable {
    File file = new File("/");
    SetSetting<File> setting = fragment().setting = mock(SetSetting.class);
    given(setting.get()).willReturn(singleton(file));

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        fragment().onResume();
      }
    });

    assertAdapterContains(file);
  }

  private void assertAdapterContains(File file) {
    for (int i = 0; i < adapter().getCount(); i++) {
      if (file.equals(adapter().getItem(i))) {
        return;
      }
    }
    fail();
  }

  private SidebarFragment fragment() {
    return getActivity().getFragment();
  }

  private ListView listView() {
    return fragment().getListView();
  }

  private SidebarAdapter adapter() {
    return fragment().getListAdapter();
  }
}
