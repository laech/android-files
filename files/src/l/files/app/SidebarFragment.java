package l.files.app;

import android.os.Bundle;
import com.squareup.otto.Subscribe;
import l.files.R;
import l.files.event.BookmarksEvent;

public final class SidebarFragment extends BaseFileListFragment {

  public SidebarFragment() {
    super(R.layout.sidebar_fragment);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setListAdapter(SidebarAdapter.get(getResources()));
  }

  @Override public SidebarAdapter getListAdapter() {
    return (SidebarAdapter) super.getListAdapter();
  }

  @Subscribe public void handle(BookmarksEvent event) {
    getListAdapter().set(event.bookmarks(), getResources());
  }
}
