package com.example.files.test;

import java.io.File;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ActionMode;

import com.example.files.R;
import com.example.files.app.FileListFragment;
import com.example.files.app.FileListFragment.OnFileSelectedListener;

public final class TestFileListFragmentActivity
        extends FragmentActivity implements OnFileSelectedListener {

    public static final String DIRECTORY = "directory";

    private FileListFragment mFragment;
    private ActionMode mMode;

    public FileListFragment getFragment() {
        return mFragment;
    }

    public ActionMode getActionMode() {
        return mMode;
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        this.mMode = mode;
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        this.mMode = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content);

        mFragment = new FileListFragment();
        mFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, mFragment)
                .commit();
    }

    @Override
    public void onFileSelected(File file) {
    }
}
