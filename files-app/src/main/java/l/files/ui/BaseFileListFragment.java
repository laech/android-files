package l.files.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.greenrobot.event.EventBus;
import l.files.common.app.BaseListFragment;
import l.files.common.widget.ListViews;
import l.files.fs.Path;
import l.files.fs.Resource;
import l.files.operations.Events;

public abstract class BaseFileListFragment
    extends BaseListFragment implements ListSelection<Resource> {

  private EventBus bus;
  private final int layoutResourceId;

  public BaseFileListFragment(int layoutResourceId) {
    this.layoutResourceId = layoutResourceId;
  }

  // TODO Review bus getter/setter is appropriate or not, possible leaks
  // when bus is changed after an object is registered?

  public EventBus getBus() {
    return bus;
  }

  public void setBus(EventBus bus) {
    this.bus = bus;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bus = Events.get();
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle state) {
    return inflater.inflate(layoutResourceId, container, false);
  }

  @Override public int getCheckedItemCount() {
    return getListView().getCheckedItemCount();
  }

  @Override public int getCheckedItemPosition() {
    return ListViews.getCheckedItemPosition(getListView());
  }

  @Override public List<Integer> getCheckedItemPositions() {
    return ListViews.getCheckedItemPositions(getListView());
  }

}
