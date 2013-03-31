package com.example.files.app;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.files.R;
import com.example.files.media.ImageMap;
import com.example.files.util.FileSystem;

final class FilesAdapter extends ArrayAdapter<Object> {

  private final FileSystem fileSystem;
  private final ImageMap images;

  FilesAdapter(Application context) {
    this(context, FileSystem.INSTANCE, ImageMap.INSTANCE);
  }

  FilesAdapter(Application context, FileSystem fileSystem, ImageMap images) {
    super(context, R.layout.files_item);
    this.fileSystem = fileSystem;
    this.images = images;
  }

  @Override public boolean isEnabled(int position) {
    return getItem(position) instanceof File;
  }

  @Override public int getItemViewType(int position) {
    return getItem(position) instanceof File ? 0 : 1;
  }

  @Override public int getViewTypeCount() {
    return 2;
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    Object item = getItem(position);
    return item instanceof File
        ? getViewForFile((File) item, view, parent)
        : getViewForHeader(item, view, parent);
  }

  private View getViewForFile(File file, View view, ViewGroup parent) {
    if (view == null) view = inflate(R.layout.files_item, parent);
    updateViewForFile(file, (TextView) view);
    return view;
  }

  private View getViewForHeader(Object header, View view, ViewGroup parent) {
    if (view == null) view = inflate(R.layout.files_item_header, parent);
    updateViewForHeader(header, (TextView) view);
    return view;
  }

  void updateViewForFile(File file, TextView view) {
    view.setEnabled(fileSystem.hasPermissionToRead(file));
    view.setText(file.getName());
    view.setCompoundDrawablesWithIntrinsicBounds(images.get(file), 0, 0, 0);
  }

  void updateViewForHeader(Object header, TextView view) {
    view.setText(header.toString());
  }

  private View inflate(int viewId, ViewGroup parent) {
    Context context = parent.getContext();
    return LayoutInflater.from(context).inflate(viewId, parent, false);
  }
}
