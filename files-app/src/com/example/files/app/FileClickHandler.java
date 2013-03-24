package com.example.files.app;

import static android.content.Intent.ACTION_VIEW;
import static android.net.Uri.fromFile;
import static android.widget.Toast.LENGTH_SHORT;
import static com.example.files.app.FileListActivity.EXTRA_DIRECTORY;
import static com.example.files.util.Objects.requires;

import java.io.File;

import android.content.ActivityNotFoundException;
import android.content.Intent;

import com.example.files.R;
import com.example.files.app.FileListFragment.OnFileSelectedListener;
import com.example.files.content.ActivityStarter;
import com.example.files.media.MediaDetector;
import com.example.files.util.FileSystem;
import com.example.files.widget.Toaster;

final class FileClickHandler implements OnFileSelectedListener, MediaDetector.Callback {

    // TODO review test and implementation

    private final ActivityStarter mActivityStarter;
    private final FileListActivity mActivity;
    private final FileSystem mFileSystem;
    private final MediaDetector mMediaDetector;
    private final Toaster mToaster;

    FileClickHandler(
            FileListActivity activity,
            FileSystem fileSystem,
            MediaDetector mediaDetector,
            ActivityStarter activityStarter,
            Toaster toaster) {

        this.mActivity = requires(activity, "activity");
        this.mActivityStarter = requires(activityStarter, "activityStarter");
        this.mMediaDetector = requires(mediaDetector, "mediaDetector");
        this.mFileSystem = requires(fileSystem, "fileSystem");
        this.mToaster = requires(toaster, "toaster");
    }

    public FileClickHandler(FileListActivity activity) {
        this(
                activity,
                FileSystem.INSTANCE,
                MediaDetector.INSTANCE,
                ActivityStarter.INSTANCE,
                Toaster.INSTANCE);
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
        mActivityStarter.startActivity(mActivity, // TODO startActivityForResult
                new Intent(mActivity, FileListActivity.class).putExtra(EXTRA_DIRECTORY, directory.getAbsolutePath()));
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
            mActivityStarter.startActivity(
                    mActivity, new Intent(ACTION_VIEW).setDataAndType(fromFile(file), type));
        } catch (ActivityNotFoundException e) {
            mToaster.toast(mActivity, R.string.no_app_to_open_file, LENGTH_SHORT);
        }
    }
}
