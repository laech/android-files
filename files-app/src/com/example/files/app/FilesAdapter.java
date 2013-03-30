package com.example.files.app;

import android.app.Application;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.files.R;
import com.example.files.media.ImageMap;
import com.example.files.util.FileSystem;

import javax.inject.Inject;
import java.io.File;

public final class FilesAdapter extends ArrayAdapter<File> {

  private final FileSystem fileSystem;
  private final ImageMap images;

  @Inject public FilesAdapter(
      Application context, FileSystem fileSystem, ImageMap images) {
    super(context, R.layout.files_item);
    this.fileSystem = fileSystem;
    this.images = images;
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
    view.setEnabled(fileSystem.hasPermissionToRead(file));
  }

  private void setIcon(View view, File file) {
    ((TextView) view)
        .setCompoundDrawablesWithIntrinsicBounds(images.get(file), 0, 0, 0);
  }

  private void setText(View view, File file) {
    ((TextView) view).setText(file.getName());
  }
}
