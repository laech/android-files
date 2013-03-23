package com.example.files.app;

import static android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;
import static android.os.Environment.getExternalStorageDirectory;
import static com.example.files.app.FragmentManagers.popAllBackStacks;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import com.example.files.R;
import com.example.files.app.FileListFragment.OnFileSelectedListener;

public class FileListActivity extends Activity
        implements OnBackStackChangedListener, OnFileSelectedListener {

    private static final File HOME = getExternalStorageDirectory();

    public static final String ARG_DIRECTORY = FileListFragment.ARG_DIRECTORY;

    private OnFileSelectedListener mFileSelectedHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content);

        mFileSelectedHandler = new FileClickHandler(this);
        getFragmentManager().addOnBackStackChangedListener(this);

        String directory = getDirectory();
        if (savedInstanceState == null) show(directory);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                updateActionBar();
            }
        });
    }

    private String getDirectory() {
        String directory = getIntent().getStringExtra(ARG_DIRECTORY);
        return directory != null ? directory : HOME.getAbsolutePath();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            popAllBackStacks(getFragmentManager());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackStackChanged() {
        updateActionBar();
    }

    @Override
    public void onFileSelected(File file) {
        mFileSelectedHandler.onFileSelected(file);
    }

    void show(String directory) {
        Bundle bundle = new Bundle(1);
        bundle.putString(ARG_DIRECTORY, directory);

        FileListFragment fragment = new FileListFragment();
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (getFileListFragment() != null) {
            transaction.addToBackStack(null).setTransition(TRANSIT_FRAGMENT_FADE);
        }

        transaction.replace(android.R.id.content, fragment).commitAllowingStateLoss();
    }

    private FileListFragment getFileListFragment() {
        return (FileListFragment) getFragmentManager().findFragmentById(android.R.id.content);
    }

    void updateActionBar() {
        updateActionBarUpButton();
        updateTitle();
    }

    private void updateActionBarUpButton() {
        ActionBar actionBar = getActionBar();
        boolean canGoUp = getFragmentManager().getBackStackEntryCount() > 0;
        actionBar.setDisplayHomeAsUpEnabled(canGoUp);
        actionBar.setHomeButtonEnabled(canGoUp);
    }

    private void updateTitle() {
        File file = getFileListFragment().getDirectory();
        setTitle(HOME.equals(file) ? getString(R.string.home) : file.getName());
    }
}
