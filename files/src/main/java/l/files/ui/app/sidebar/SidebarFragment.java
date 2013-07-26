package l.files.ui.app.sidebar;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import l.files.R;
import l.files.event.BookmarksEvent;
import l.files.event.OpenFileRequest;

import java.io.File;

import static l.files.event.Events.bus;

public final class SidebarFragment extends ListFragment {

  Bus bus = bus();

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setListAdapter(SidebarAdapter.get(getResources()));
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.sidebar_fragment, container, false);
  }

  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    Object item = l.getItemAtPosition(pos);
    if (item instanceof File) {
      bus.post(new OpenFileRequest((File) item));
    }
  }

  @Override public SidebarAdapter getListAdapter() {
    return (SidebarAdapter) super.getListAdapter();
  }

  @Subscribe public void handle(BookmarksEvent event) {
    getListAdapter().set(event.bookmarks(), getResources());
  }

}
