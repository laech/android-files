package com.example.files.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import com.example.files.R;
import com.example.files.event.FileSelectedEvent;
import com.example.files.event.MediaDetectedEvent;
import com.example.files.media.MediaDetector;
import com.example.files.util.FileSystem;
import com.example.files.widget.Toaster;

import javax.inject.Inject;
import java.io.File;

import static android.content.Intent.ACTION_VIEW;
import static android.net.Uri.fromFile;
import static android.widget.Toast.LENGTH_SHORT;
import static com.example.files.app.FilesActivity.EXTRA_DIRECTORY;

public class FilesActivityHelper {

  private final Toaster toaster;
  private final FileSystem fileSystem;
  private final MediaDetector mediaDetector;

  @Inject public FilesActivityHelper(
      FileSystem fileSystem, MediaDetector mediaDetector, Toaster toaster) {
    this.toaster = toaster;
    this.fileSystem = fileSystem;
    this.mediaDetector = mediaDetector;
  }

  public void handle(FileSelectedEvent event, FilesActivity activity) {
    File file = event.file();
    if (!fileSystem.hasPermissionToRead(file)) {
      showPermissionDenied(activity);
    } else if (file.isDirectory()) {
      showDirectory(file, activity);
    } else {
      showFile(file);
    }
  }

  private void showPermissionDenied(FilesActivity activity) {
    toaster.toast(activity, R.string.permission_denied, LENGTH_SHORT);
  }

  private void showDirectory(File directory, FilesActivity activity) {
    int requestCode = 0;
    Intent intent = newShowDirectoryIntent(directory, activity);
    activity.startActivityForResult(intent, requestCode);
    activity.overridePendingTransition(R.anim.activity_appear, R.anim.still);
  }

  private Intent newShowDirectoryIntent(File dir, FilesActivity activity) {
    return new Intent(activity, FilesActivity.class)
        .putExtra(EXTRA_DIRECTORY, dir.getAbsolutePath());
  }

  private void showFile(File file) {
    mediaDetector.detect(file);
  }

  public void handle(MediaDetectedEvent event, FilesActivity activity) {
    if (!event.mediaType().isPresent()) {
      toaster.toast(activity, R.string.unknown_file_type, LENGTH_SHORT);
    } else {
      showFile(event.file(), event.mediaType().get(), activity);
    }
  }

  private void showFile(File file, String mediaType, FilesActivity activity) {
    try {
      activity.startActivity(newShowFileIntent(file, mediaType));
    } catch (ActivityNotFoundException e) {
      toaster.toast(activity, R.string.no_app_to_open_file, LENGTH_SHORT);
    }
  }

  private Intent newShowFileIntent(File file, String mediaType) {
    return new Intent(ACTION_VIEW).setDataAndType(fromFile(file), mediaType);
  }
}