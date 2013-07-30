package l.files.ui.app.sidebar;

import android.os.Bundle;
import com.squareup.otto.Subscribe;
import l.files.R;
import l.files.event.BookmarksEvent;
import l.files.ui.app.BaseFileListFragment;

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
