package l.files.ui.app.sidebar;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.util.Collections.sort;
import static l.files.BuildConfig.DEBUG;
import static l.files.FilesApp.getApp;
import static l.files.ui.UserDirs.DIR_HOME;
import static l.files.ui.UserDirs.DIR_ROOT;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import l.files.FilesApp;
import l.files.R;
import l.files.Settings;
import l.files.ui.FileDrawableProvider;
import l.files.ui.FileLabelProvider;
import l.files.ui.event.FileSelectedEvent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.common.base.Function;
import com.squareup.otto.Bus;

public final class SidebarFragment
    extends ListFragment implements OnSharedPreferenceChangeListener {

  SidebarAdapter adapter;
  Settings settings;
  Bus bus;

  private long favoritesUpdatedTimestamp;
  
  private Function<File, String> labels;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    labels = new FileLabelProvider(getResources());
    bus = FilesApp.BUS;
    settings = getApp(this).getSettings();
    adapter = new SidebarAdapter(getApp(this),
        new FileDrawableProvider(getResources()),
        labels) {
      @Override protected int getItemTextViewResourceId() {
        return R.layout.sidebar_item;
      }
    }; // TODO fix?
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
    adapter.add(getString(R.string.bookmarks));
    adapter.addAll(getBookmarks());
    adapter.add(getString(R.string.device));
    adapter.add(DIR_HOME);
    adapter.add(DIR_ROOT);
    adapter.notifyDataSetChanged();
  }

  private Collection<File> getBookmarks() {
    Set<String> paths = settings.getBookmarks();
    List<File> dirs = newArrayListWithCapacity(paths.size());
    for (String path : paths) {
      File f = new File(path);
      if (f.isDirectory() && f.canRead()) dirs.add(f);
    }
    sort(dirs, new Comparator<File>() {
      @Override public int compare(File a, File b) {
        String x = nullToEmpty(labels.apply(a));
        String y = nullToEmpty(labels.apply(b));
        return x.compareToIgnoreCase(y);
      }
    });
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
