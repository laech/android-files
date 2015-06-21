package l.files.common.widget;

import android.view.View;

import static android.support.v4.widget.DrawerLayout.DrawerListener;

public final class CompositeDrawerListener implements DrawerListener
{

    private final DrawerListener[] listeners;

    public CompositeDrawerListener(final DrawerListener... listeners)
    {
        this.listeners = listeners.clone();
    }

    @Override
    public void onDrawerSlide(final View drawerView, final float slideOffset)
    {
        for (final DrawerListener listener : listeners)
        {
            listener.onDrawerSlide(drawerView, slideOffset);
        }
    }

    @Override
    public void onDrawerOpened(final View drawerView)
    {
        for (final DrawerListener listener : listeners)
        {
            listener.onDrawerOpened(drawerView);
        }
    }

    @Override
    public void onDrawerClosed(final View drawerView)
    {
        for (final DrawerListener listener : listeners)
        {
            listener.onDrawerClosed(drawerView);
        }
    }

    @Override
    public void onDrawerStateChanged(final int newState)
    {
        for (final DrawerListener listener : listeners)
        {
            listener.onDrawerStateChanged(newState);
        }
    }
}
