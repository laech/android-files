package com.example.files.app;

import static com.example.files.BuildConfig.DEBUG;
import static com.example.files.app.FilesApp.getApp;
import static com.example.files.util.FileSystem.DIRECTORY_HOME;
import static com.example.files.util.FileSystem.DIRECTORY_ROOT;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.files.R;
import com.example.files.event.FileSelectedEvent;
import com.example.files.util.FileSystem;
import com.squareup.otto.Bus;

public final class SidebarFragment
    extends ListFragment implements OnSharedPreferenceChangeListener {

  FileSystem fileSystem;
  FilesAdapter adapter;
  Settings settings;
  Bus bus;

  private long favoritesUpdatedTimestamp;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fileSystem = FileSystem.INSTANCE;
    bus = FilesApp.BUS;
    settings = getApp(this).getSettings();
    adapter = new FilesAdapter(getApp(this));
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    refresh();
    setListAdapter(adapter);
  }

  void refresh() {
    if (DEBUG) Log.d("SidebarFragment", "refresh");
    favoritesUpdatedTimestamp = settings.getFavoritesUpdatedTimestamp();
    adapter.setNotifyOnChange(false);
    adapter.clear();
    adapter.add(getString(R.string.favorites));
    adapter.addAll(getFavorites());
    adapter.add(getString(R.string.device));
    adapter.add(DIRECTORY_HOME);
    adapter.add(DIRECTORY_ROOT);
    adapter.notifyDataSetChanged();
  }

  private Collection<File> getFavorites() {
    Set<String> paths = settings.getFavorites();
    Set<File> dirs = newHashSetWithExpectedSize(paths.size());
    for (String path : paths) {
      File f = new File(path);
      if (f.isDirectory() && fileSystem.hasPermissionToRead(f)) dirs.add(f);
    }
    return dirs;
  }

  @Override public void onResume() {
    super.onResume();
    settings.getPreferences().registerOnSharedPreferenceChangeListener(this);
    long timestamp = settings.getFavoritesUpdatedTimestamp();
    if (favoritesUpdatedTimestamp != timestamp) refresh();
  }

  @Override public void onPause() {
    super.onPause();
    settings.getPreferences().unregisterOnSharedPreferenceChangeListener(this);
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

  @Override public void onSharedPreferenceChanged(
      SharedPreferences preferences, String key) {
    if (settings.getFavoritesUpdatedTimestampKey().equals(key)) refresh();
  }
}
