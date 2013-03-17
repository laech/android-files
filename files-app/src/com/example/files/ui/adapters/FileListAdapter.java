package com.example.files.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.files.R;
import com.example.files.media.ImageMap;
import com.example.files.util.FileSystem;

import java.io.File;

import static com.example.files.util.Objects.requires;

public final class FileListAdapter extends ArrayAdapter<File> {

  private final FileSystem fs;
  private final ImageMap images;

  public FileListAdapter(Context context) {
    this(context, new FileSystem(), new ImageMap());
  }

  public FileListAdapter(Context context, FileSystem fs, ImageMap images) {
    super(context, R.layout.file_item);
    this.fs = requires(fs, "fs");
    this.images = requires(images, "images");
  }

  @Override public boolean isEnabled(int position) {
    return fs.hasPermissionToRead(getItem(position));
  }

  @Override public View getView(int position, View v, ViewGroup parent) {
    return updateView(super.getView(position, v, parent), getItem(position));
  }

  View updateView(View view, File file) {
    setEnabled(view, file);
    setText(view, file);
    setIcon(view, file);
    return view;
  }

  private void setEnabled(View view, File file) {
    view.setEnabled(fs.hasPermissionToRead(file));
  }

  private void setIcon(View view, File file) {
    ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(
        images.get(file), 0, 0, 0);
  }

  private void setText(View view, File file) {
    ((TextView) view).setText(file.getName());
  }
}
