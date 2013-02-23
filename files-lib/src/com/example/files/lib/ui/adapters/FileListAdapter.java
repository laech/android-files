package com.example.files.lib.ui.adapters;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.files.lib.R;

public final class FileListAdapter extends ArrayAdapter<File> {

  public FileListAdapter(Context context, File[] files) {
    super(context, R.layout.file_item, checkNotNull(files, "files"));

    for (File file : files) {
      checkNotNull(file, "file");
    }
  }

  @Override public View getView(int position, View v, ViewGroup parent) {
    View view = super.getView(position, v, parent);

    File file = getItem(position);
    setEnabled(view, file);
    setText(view, file);
    setIcon(view, file);

    return view;
  }

  private void setEnabled(View view, File file) {
    view.setEnabled(file.canRead()); // TODO review
  }

  private void setIcon(View view, File file) {
    ((TextView)view).setCompoundDrawablesWithIntrinsicBounds(
        file.isDirectory() ? R.drawable.ic_dir : 0, 0, 0, 0); // TODO
  }

  private void setText(View view, File file) {
    ((TextView)view).setText(file.getName()); // TODO review
  }
}
