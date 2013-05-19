package l.files.ui.util;

import android.app.FragmentManager;
import junit.framework.TestCase;

import static l.files.ui.util.FragmentManagers.popAllBackStacks;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public final class FragmentManagersTest extends TestCase {

  public void testPopsAllBackStacks() {
    FragmentManager fm = mock(FragmentManager.class);
    given(fm.getBackStackEntryCount()).willReturn(10);

    popAllBackStacks(fm);

    verify(fm, times(10)).popBackStack();
  }
}
