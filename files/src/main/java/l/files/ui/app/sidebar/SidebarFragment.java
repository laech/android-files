package l.files.ui.app.sidebar;

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
import l.files.FilesApp;
import l.files.R;
import l.files.setting.SetSetting;
import l.files.ui.event.FileSelectedEvent;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;
import static l.files.BuildConfig.DEBUG;
import static l.files.setting.Settings.getBookmarksSetting;
import static l.files.ui.Labels.newFileDrawableProvider;
import static l.files.ui.Labels.newFileLabelProvider;
import static l.files.ui.UserDirs.DIR_HOME;
import static l.files.ui.UserDirs.DIR_ROOT;

public final class SidebarFragment
    extends ListFragment implements OnSharedPreferenceChangeListener {

  SidebarAdapter adapter;
  SetSetting<File> setting;
  Bus bus;

  private Set<File> bookmarks;

  private Function<File, String> labels;

  private SharedPreferences pref;

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    labels = newFileLabelProvider(getResources());
    bus = FilesApp.BUS;
    pref = getDefaultSharedPreferences(getActivity());
    setting = getBookmarksSetting(pref);
    adapter = new SidebarAdapter(getActivity(),
        newFileDrawableProvider(getResources()),
        labels) {
      @Override protected int getItemTextViewResourceId() {
        return R.layout.sidebar_item;
      }
    }; // TODO fix?
    refresh();
    setListAdapter(adapter);
  }

  void refresh() {
    if (DEBUG) Log.d("SidebarFragment", "refresh");
    bookmarks = setting.get();
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
    List<File> dirs = newArrayList(setting.get());
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
    pref.registerOnSharedPreferenceChangeListener(this);
    Set<File> files = setting.get();
    if (files.equals(bookmarks)) {
      refresh();
    }
  }

  @Override public void onPause() {
    super.onPause();
    pref.unregisterOnSharedPreferenceChangeListener(this);
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
    if (setting.key().equals(key)) refresh();
  }
}
