package com.example.files.app;

import static android.content.Intent.ACTION_VIEW;
import static android.net.Uri.fromFile;
import static android.os.Environment.getExternalStorageDirectory;
import static android.text.TextUtils.isEmpty;
import static android.widget.Toast.LENGTH_SHORT;
import static com.example.files.app.FileListPagerAdapter.POSITION_FILE_LIST;
import static com.example.files.util.Objects.requires;

import java.io.File;

import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.app.FileListFragment.OnFileSelectedListener;
import com.example.files.media.MediaDetector;
import com.example.files.util.FileSystem;
import com.example.files.widget.Toaster;

public class FileListActivity extends FragmentActivity implements OnFileSelectedListener {

    final static class FileClickHandler implements OnFileSelectedListener, MediaDetector.Callback {

        private final FileListActivity mActivity;
        private final FileSystem mFileSystem;
        private final MediaDetector mMediaDetector;
        private final Toaster mToaster;

        FileClickHandler(
                FileListActivity activity,
                FileSystem fileSystem,
                MediaDetector mediaDetector,
                Toaster toaster) {

            this.mActivity = requires(activity, "activity");
            this.mMediaDetector = requires(mediaDetector, "mediaDetector");
            this.mFileSystem = requires(fileSystem, "fileSystem");
            this.mToaster = requires(toaster, "toaster");
        }

        public FileClickHandler(FileListActivity activity) {
            this(activity, FileSystem.INSTANCE, MediaDetector.INSTANCE, Toaster.INSTANCE);
        }

        @Override
        public void onFileSelected(File file) {
            if (!mFileSystem.hasPermissionToRead(file)) {
                showPermissionDenied();
            } else if (file.isDirectory()) {
                showDirectory(file);
            } else {
                showFile(file);
            }
        }

        private void showFile(File file) {
            mMediaDetector.detect(file, this);
        }

        private void showDirectory(File directory) {
            mActivity.startActivityForResult(new Intent(mActivity, FileListActivity.class)
                    .putExtra(EXTRA_DIRECTORY, directory.getAbsolutePath()), 0);
            mActivity.overridePendingTransition(R.anim.activity_appear, R.anim.still);
        }

        private void showPermissionDenied() {
            mToaster.toast(mActivity, R.string.permission_denied, LENGTH_SHORT);
        }

        @Override
        public void onResult(File file, String type) {
            if (type == null) {
                mToaster.toast(mActivity, R.string.unknown_file_type, LENGTH_SHORT);
                return;
            }

            try {
                mActivity.startActivity(new Intent(ACTION_VIEW)
                        .setDataAndType(fromFile(file), type));
            } catch (ActivityNotFoundException e) {
                mToaster.toast(mActivity, R.string.no_app_to_open_file, LENGTH_SHORT);
            }
        }
    }

    public static final String EXTRA_DIRECTORY = FileListFragment.ARG_DIRECTORY;

    private static final int RESULT_HOME_PRESSED = 100;
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
        setTitle(mIsHome ? getString(R.string.home) : directory.getName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            showHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!mIsHome && resultCode == RESULT_HOME_PRESSED) showHome();
    }

    private void showHome() {
        setResult(RESULT_HOME_PRESSED);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.still, R.anim.activity_disappear);
    }

    @Override
    public void onFileSelected(File file) {
        mFileSelectedHandler.onFileSelected(file);
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        findCustomTitleView().setText(title);
    }

    private TextView findCustomTitleView() {
        return (TextView)findViewById(R.id.action_bar_title);
    }
}
