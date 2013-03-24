package com.example.files.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FileListPagerAdapter extends FragmentPagerAdapter {

    // TODO experimental

    public static final int POSITION_MENU = 0;
    public static final int POSITION_FILE_LIST = 1;

    private FileListFragment mFileListFragment;
    private final String mDirectory;

    public FileListPagerAdapter(FragmentManager fm, String directory) {
        super(fm);
        mDirectory = directory;
    }

    @Override
    public float getPageWidth(int position) {
        if (position == POSITION_MENU) {
            return 0.75f;
        }
        return super.getPageWidth(position);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
        case POSITION_FILE_LIST: {
            Bundle args = new Bundle(1);
            args.putString(FileListFragment.ARG_DIRECTORY, mDirectory == null ? "/" : mDirectory);
            fragment = mFileListFragment = new FileListFragment();
            fragment.setArguments(args);
            break;
        }
        default: {
            Bundle args = new Bundle(1);
            args.putString(FileListFragment.ARG_DIRECTORY, "/");
            fragment = new FileListFragment();
            fragment.setArguments(args);
            break;
        }
        }
        return fragment;
    }

    public FileListFragment getFileListFragment() {
        return mFileListFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

}
