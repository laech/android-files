package com.example.files.app;

import android.app.FragmentManager;
import junit.framework.TestCase;

import static com.example.files.app.FragmentManagers.popAllBackStacks;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public final class FragmentManagersTest extends TestCase {

  public void testPopsAllBackStacks() {
    FragmentManager fm = mock(FragmentManager.class);
    given(fm.getBackStackEntryCount()).willReturn(10);

    popAllBackStacks(fm);

    verify(fm, times(10)).popBackStack();
  }
}
