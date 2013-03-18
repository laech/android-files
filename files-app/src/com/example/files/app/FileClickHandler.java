package com.example.files.app;

import static android.content.Intent.ACTION_VIEW;
import static android.net.Uri.fromFile;
import static android.widget.Toast.LENGTH_SHORT;
import static com.example.files.util.Files.getFileExtension;
import static com.example.files.util.Objects.requires;

import java.io.File;

import android.content.ActivityNotFoundException;
import android.content.Intent;

import com.example.files.R;
import com.example.files.app.FileListFragment.OnFileSelectedListener;
import com.example.files.content.ActivityStarter;
import com.example.files.media.MediaMap;
import com.example.files.util.FileSystem;
import com.example.files.widget.Toaster;

final class FileClickHandler implements OnFileSelectedListener {

  private final FileListActivity activity;
  private final FileSystem fs;
  private final MediaMap media;
  private final ActivityStarter starter;
  private final Toaster toaster;

  FileClickHandler(
      FileListActivity activity,
      FileSystem fs,
      MediaMap media,
      ActivityStarter starter,
      Toaster toaster) {

    this.activity = requires(activity, "activity");
    this.media = requires(media, "media");
    this.fs = requires(fs, "fs");
    this.starter = requires(starter, "start");
    this.toaster = requires(toaster, "toaster");
  }

  public FileClickHandler(FileListActivity activity) {
    this(activity,
        FileSystem.INSTANCE,
        MediaMap.INSTANCE,
        ActivityStarter.INSTANCE,
        Toaster.INSTANCE);
  }

  @Override public void onFileSelected(File file) {
    if (!fs.hasPermissionToRead(file)) {
      showPermissionDenied();
    } else if (file.isDirectory()) {
      showDirectory(file);
    } else {
      showFile(file);
    }
  }

  private void showFile(File file) {
    String type = media.get(getFileExtension(file));
    if (type == null) {
      toaster.toast(activity, R.string.unknown_file_type, LENGTH_SHORT);
      return;
    }

    try {
      starter.startActivity(activity, new Intent(ACTION_VIEW)
          .setDataAndType(fromFile(file), type));
    } catch (ActivityNotFoundException e) {
      toaster.toast(activity, R.string.no_app_to_open_file, LENGTH_SHORT);
    }
  }

  private void showDirectory(File directory) {
    activity.show(directory.getAbsolutePath());
  }

  private void showPermissionDenied() {
    toaster.toast(activity, R.string.permission_denied, LENGTH_SHORT);
  }
}
