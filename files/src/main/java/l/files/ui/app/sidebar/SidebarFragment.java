package l.files.ui.app.sidebar;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.squareup.otto.Bus;
import l.files.R;
import l.files.setting.SetSetting;
import l.files.ui.event.FileSelectedEvent;

import java.io.File;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static l.files.event.Events.bus;
import static l.files.setting.Settings.getBookmarksSetting;

public final class SidebarFragment
    extends ListFragment implements OnSharedPreferenceChangeListener {

  Bus bus;
  SetSetting<File> setting;
  SharedPreferences pref;

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    bus = bus();
    pref = getDefaultSharedPreferences(getActivity());
    setting = getBookmarksSetting(pref);

    setListAdapter(SidebarAdapter.get(getResources()));
  }

  @Override public void onResume() {
    super.onResume();
    pref.registerOnSharedPreferenceChangeListener(this);
    refresh();
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
    if (item instanceof File) {
      bus.post(new FileSelectedEvent((File) item));
    }
  }

  @Override public void onSharedPreferenceChanged(
      SharedPreferences preferences, String key) {
    if (setting.key().equals(key)) refresh();
  }

  @Override public SidebarAdapter getListAdapter() {
    return (SidebarAdapter) super.getListAdapter();
  }

  private void refresh() {
    getListAdapter().set(setting, getResources());
  }

}
