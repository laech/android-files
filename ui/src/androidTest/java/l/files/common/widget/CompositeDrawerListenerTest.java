package l.files.common.widget;

import android.view.View;

import l.files.testing.BaseTest;

import static android.support.v4.widget.DrawerLayout.DrawerListener;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class CompositeDrawerListenerTest extends BaseTest {

    private DrawerListener delegate1;
    private DrawerListener delegate2;
    private DrawerListener composite;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        delegate1 = mock(DrawerListener.class);
        delegate2 = mock(DrawerListener.class);
        composite = new CompositeDrawerListener(delegate1, delegate2);
    }

    public void testOnDrawerSlideIsDelegated() throws Exception {
        View view = mock(View.class);
        composite.onDrawerSlide(view, 1.1f);
        verify(delegate1).onDrawerSlide(view, 1.1f);
        verify(delegate2).onDrawerSlide(view, 1.1f);
    }

    public void testOnDrawerOpenedIsDelegated() throws Exception {
        View view = mock(View.class);
        composite.onDrawerOpened(view);
        verify(delegate1).onDrawerOpened(view);
        verify(delegate2).onDrawerOpened(view);
    }

    public void testOnDrawerClosedIsDelegated() throws Exception {
        View view = mock(View.class);
        composite.onDrawerClosed(view);
        verify(delegate1).onDrawerClosed(view);
        verify(delegate2).onDrawerClosed(view);
    }

    public void testOnDrawerStateChangedIsDelegated() throws Exception {
        composite.onDrawerStateChanged(101);
        verify(delegate1).onDrawerStateChanged(101);
        verify(delegate2).onDrawerStateChanged(101);
    }
}
