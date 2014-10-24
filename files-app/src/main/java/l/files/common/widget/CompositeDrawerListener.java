package l.files.common.widget;

import android.view.View;

import com.google.common.collect.ImmutableList;

import static android.support.v4.widget.DrawerLayout.DrawerListener;

final class CompositeDrawerListener implements DrawerListener {

  private final DrawerListener[] listeners;

  CompositeDrawerListener(DrawerListener... listeners) {
    this.listeners = ImmutableList.copyOf(listeners)
        .toArray(new DrawerListener[listeners.length]);
  }

  @Override public void onDrawerSlide(View drawerView, float slideOffset) {
    for (DrawerListener listener : listeners) {
      listener.onDrawerSlide(drawerView, slideOffset);
    }
  }

  @Override public void onDrawerOpened(View drawerView) {
    for (DrawerListener listener : listeners) {
      listener.onDrawerOpened(drawerView);
    }
  }

  @Override public void onDrawerClosed(View drawerView) {
    for (DrawerListener listener : listeners) {
      listener.onDrawerClosed(drawerView);
    }
  }

  @Override public void onDrawerStateChanged(int newState) {
    for (DrawerListener listener : listeners) {
      listener.onDrawerStateChanged(newState);
    }
  }
}
