package com.example.files.app;

import static com.example.files.app.FilesApp.getApp;
import static com.google.common.collect.Lists.newArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.files.R;

public final class SidebarFragment extends ListFragment {

  FilesAdapter adapter;
  Settings settings;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    settings = getApp(this).getSettings();
    adapter = new FilesAdapter(getApp(this));
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    adapter.addAll(newArrayList(settings.getFavoriteFiles()));
    setListAdapter(adapter);
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.sidebar_fragment, container, false);
  }
}
