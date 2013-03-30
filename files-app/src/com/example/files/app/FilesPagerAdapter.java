package com.example.files.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FilesPagerAdapter extends FragmentPagerAdapter {

  // TODO experimental

  public static final int POSITION_MENU = 0;
  public static final int POSITION_FILE_LIST = 1;

  private final boolean portrait;
  private final String directory;

  public FilesPagerAdapter(FragmentManager fm, String dir, boolean portait) {
    super(fm);
    this.directory = dir;
    this.portrait = portait;
  }

  @Override public float getPageWidth(int position) {
    if (position == POSITION_MENU) {
      return portrait ? 0.618f : 0.382f;
    }
    return super.getPageWidth(position);
  }

  @Override public Fragment getItem(int position) {
    switch (position) {
      case POSITION_FILE_LIST:
        Bundle args = new Bundle(1);
        args.putString(FilesFragment.ARG_DIRECTORY, directory == null ? "/" : directory);
        Fragment fragment = new FilesFragment();
        fragment.setArguments(args);
        return fragment;

      default:
        return new SidebarFragment();
    }
  }

  @Override public int getCount() {
    return 2;
  }

}
