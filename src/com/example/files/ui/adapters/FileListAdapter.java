package com.example.files.ui.adapters;

import static com.example.files.util.Objects.requires;

import java.io.File;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.util.FileSystem;

public final class FileListAdapter extends ArrayAdapter<File> {

  private final FileSystem fs;

  public FileListAdapter(Context context, File[] files, FileSystem fs) {
    super(context, R.layout.file_item, requires(files, "files"));
    this.fs = requires(fs, "fs");
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
    view.setEnabled(fs.hasPermissionToRead(file)); // TODO review
  }

  private void setIcon(View view, File file) {
    ((TextView)view).setCompoundDrawablesWithIntrinsicBounds(
        file.isDirectory() ? R.drawable.ic_folder : R.drawable.ic_file,
        0, 0, 0); // TODO
  }

  private void setText(View view, File file) {
    ((TextView)view).setText(file.getName());
  }
}
