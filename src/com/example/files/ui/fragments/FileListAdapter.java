package com.example.files.ui.fragments;

import java.io.File;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.files.R;

public final class FileListAdapter extends ArrayAdapter<File> {

  public FileListAdapter(Context context, File[] files) {
    super(context, R.layout.file_item, files);
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    view = super.getView(position, view, parent);
    ((TextView)view).setText(getItem(position).getName());
    return view;
  }
}
