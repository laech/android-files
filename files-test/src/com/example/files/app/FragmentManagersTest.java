package com.example.files.app;

import static com.example.files.app.FragmentManagers.popAllBackStacks;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import junit.framework.TestCase;
import android.app.FragmentManager;

public final class FragmentManagersTest extends TestCase {

    public void testPopsAllBackStacks() {
        FragmentManager fm = mock(FragmentManager.class);
        given(fm.getBackStackEntryCount()).willReturn(10);

        popAllBackStacks(fm);

        verify(fm, times(10)).popBackStack();
    }
}
