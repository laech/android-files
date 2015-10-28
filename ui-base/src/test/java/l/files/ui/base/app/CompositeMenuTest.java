package l.files.ui.base.app;

import android.view.Menu;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class CompositeMenuTest {

    private OptionsMenu action1;
    private OptionsMenu action2;
    private CompositeMenu composite;

    @Before
    public void setUp() throws Exception {
        action1 = mock(OptionsMenu.class);
        action2 = mock(OptionsMenu.class);
        composite = new CompositeMenu(action1, action2);
    }

    @Test
    public void onCreateIsDelegated() {
        Menu menu = mock(Menu.class);
        composite.onCreateOptionsMenu(menu);
        verify(action1).onCreateOptionsMenu(menu);
        verify(action2).onCreateOptionsMenu(menu);
    }

    @Test
    public void onPrepareIsDelegated() {
        Menu menu = mock(Menu.class);
        composite.onPrepareOptionsMenu(menu);
        verify(action1).onPrepareOptionsMenu(menu);
        verify(action2).onPrepareOptionsMenu(menu);
    }
}
