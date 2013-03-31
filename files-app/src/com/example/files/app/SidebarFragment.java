package com.example.files.app;

import static com.example.files.app.FilesApp.getApp;
import static com.example.files.util.FileSystem.DIRECTORY_HOME;
import static com.example.files.util.FileSystem.DIRECTORY_ROOT;

import java.io.File;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.files.R;
import com.example.files.event.FileSelectedEvent;
import com.squareup.otto.Bus;

public final class SidebarFragment extends ListFragment {

  FilesAdapter adapter;
  Settings settings;
  Bus bus;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bus = FilesApp.BUS;
    settings = getApp(this).getSettings();
    adapter = new FilesAdapter(getApp(this));
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    adapter.add(getString(R.string.favorites));
    adapter.addAll(settings.getFavoriteFiles());
    adapter.add(getString(R.string.device));
    adapter.add(DIRECTORY_HOME);
    adapter.add(DIRECTORY_ROOT);
    setListAdapter(adapter);
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.sidebar_fragment, container, false);
  }

  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    Object item = l.getItemAtPosition(pos);
    if (item instanceof File) bus.post(new FileSelectedEvent((File) item));
  }
}
