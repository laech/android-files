package com.example.files.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FilesPagerAdapter extends FragmentPagerAdapter {

  // TODO experimental

  public static final int POSITION_MENU = 0;
  public static final int POSITION_FILE_LIST = 1;

  private final String directory;

  public FilesPagerAdapter(FragmentManager fm, String directory) {
    super(fm);
    this.directory = directory;
  }

  @Override public float getPageWidth(int position) {
    if (position == POSITION_MENU) {
      return 0.75f;
    }
    return super.getPageWidth(position);
  }

  @Override public Fragment getItem(int position) {
    Fragment fragment;
    switch (position) {
      case POSITION_FILE_LIST: {
        Bundle args = new Bundle(1);
        args.putString(FilesFragment.ARG_DIRECTORY, directory == null ? "/" : directory);
        fragment = new FilesFragment();
        fragment.setArguments(args);
        break;
      }
      default: {
        Bundle args = new Bundle(1);
        args.putString(FilesFragment.ARG_DIRECTORY, "/");
        fragment = new FilesFragment();
        fragment.setArguments(args);
        break;
      }
    }
    return fragment;
  }

  @Override public int getCount() {
    return 2;
  }

}
