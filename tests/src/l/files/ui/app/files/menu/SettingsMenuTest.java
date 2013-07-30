package l.files.ui.app.files.menu;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import junit.framework.TestCase;
import l.files.R;
import l.files.ui.app.settings.SettingsActivity;
import org.mockito.ArgumentCaptor;

import static android.view.Menu.CATEGORY_SECONDARY;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.test.Mocks.mockMenuItem;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class SettingsMenuTest extends TestCase {

  private Context context;
  private SettingsMenu action;

  @Override protected void setUp() throws Exception {
    super.setUp();
    context = mockContext();
    action = new SettingsMenu(context);
  }

  private Context mockContext() {
    Context context = mock(Context.class);
    given(context.getPackageName()).willReturn("test");
    return context;
  }

  public void testOnCreate() {
    MenuItem item = mockMenuItem();
    Menu menu = mock(Menu.class);
    given(callAddMenuItem(menu)).willReturn(item);

    action.onCreate(menu);

    callAddMenuItem(verify(menu));
    verify(item).setShowAsAction(SHOW_AS_ACTION_NEVER);
    verifyCapturedIntent(item);
  }

  private void verifyCapturedIntent(MenuItem item) {
    ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
    verify(item).setIntent(captor.capture());

    assertThat(captor.getValue().getComponent())
        .isEqualTo(new ComponentName(context, SettingsActivity.class));
  }

  private MenuItem callAddMenuItem(Menu menu) {
    return menu.add(NONE, R.id.settings, CATEGORY_SECONDARY, R.string.settings);
  }
}
