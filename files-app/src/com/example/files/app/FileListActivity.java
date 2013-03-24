package com.example.files.app;

import static android.os.Environment.getExternalStorageDirectory;
import static android.text.TextUtils.isEmpty;
import static com.example.files.app.FileListPagerAdapter.POSITION_FILE_LIST;

import java.io.File;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.example.files.R;
import com.example.files.app.FileListFragment.OnFileSelectedListener;

public class FileListActivity extends FragmentActivity implements OnFileSelectedListener {

    // TODO review test and implementation

    public static final String EXTRA_DIRECTORY = FileListFragment.ARG_DIRECTORY;

    private static final File HOME = getExternalStorageDirectory();

    private OnFileSelectedListener mFileSelectedHandler;

    private boolean mIsHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String path = getIntent().getStringExtra(EXTRA_DIRECTORY);
        mIsHome = isEmpty(path);
        mFileSelectedHandler = new FileClickHandler(this);

        File directory = mIsHome ? HOME : new File(path);
        setContentView(createViewPager(directory));
        updateActionBar(directory);
    }

    private ViewPager createViewPager(File directory) {
        FragmentManager fm = getSupportFragmentManager();
        ViewPager pager = new ViewPager(this);
        pager.setId(R.id.content);
        pager.setAdapter(new FileListPagerAdapter(fm, directory.getAbsolutePath()));
        pager.setCurrentItem(POSITION_FILE_LIST);
        return pager;
    }

    private void updateActionBar(File directory) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(!mIsHome);
        actionBar.setHomeButtonEnabled(!mIsHome);
        actionBar.setTitle(mIsHome ? getString(R.string.home) : directory.getName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!mIsHome) finish();
    }

    @Override
    public void onFileSelected(File file) {
        mFileSelectedHandler.onFileSelected(file);
    }
}
