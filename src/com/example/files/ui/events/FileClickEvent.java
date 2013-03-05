package com.example.files.ui.events;

import static com.example.files.util.Objects.requires;

import java.io.File;

import android.app.Activity;

public final class FileClickEvent {

  private final File file;
  private final Activity activity;

  public FileClickEvent(Activity activity, File file) {
    this.activity = requires(activity, "activity");
    this.file = requires(file, "file");
  }

  public Activity getActivity() {
    return activity;
  }

  public File getFile() {
    return file;
  }
}
