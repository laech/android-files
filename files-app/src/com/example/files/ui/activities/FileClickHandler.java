package com.example.files.ui.activities;

import static android.content.Intent.ACTION_VIEW;
import static android.net.Uri.fromFile;
import static android.widget.Toast.LENGTH_SHORT;
import static com.example.files.ui.activities.FileListActivity.ARG_DIRECTORY;
import static com.example.files.util.Files.getFileExtension;
import static com.example.files.util.Objects.requires;

import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import com.example.files.R;
import com.example.files.media.MediaMap;
import com.example.files.ui.ActivityStarter;
import com.example.files.ui.Toaster;
import com.example.files.ui.fragments.FileListFragment.FileClickListener;
import com.example.files.util.FileSystem;

public final class FileClickHandler implements FileClickListener {

  private final FileSystem fs;
  private final MediaMap media;
  private final ActivityStarter starter;
  private final Toaster toaster;

  public FileClickHandler( // TODO
      FileSystem fs,
      MediaMap media,
      ActivityStarter starter,
      Toaster toaster) {

    this.media = requires(media, "media");
    this.fs = requires(fs, "fs");
    this.starter = requires(starter, "start");
    this.toaster = requires(toaster, "toaster");
  }

  public FileClickHandler() {
    this(new FileSystem(), new MediaMap(), new ActivityStarter(), new Toaster());
  }

  @Override public void onFileClick(Activity activity, File file) {
    if (!fs.hasPermissionToRead(file)) {
      showPermissionDenied(activity);
    } else if (file.isDirectory()) {
      showDirectory(activity, file);
    } else {
      showFile(activity, file);
    }
  }

  private void showFile(Activity activity, File file) {
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

  private void showDirectory(Context context, File directory) {
    starter.startActivity(context, new Intent(context, FileListActivity.class)
        .putExtra(ARG_DIRECTORY, directory.getAbsolutePath()));
  }

  private void showPermissionDenied(Context context) {
    toaster.toast(context, R.string.permission_denied, LENGTH_SHORT);
  }
}
