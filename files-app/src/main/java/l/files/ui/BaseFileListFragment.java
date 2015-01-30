package l.files.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.greenrobot.event.EventBus;
import l.files.common.app.BaseListFragment;
import l.files.operations.Events;

class BaseFileListFragment extends BaseListFragment {

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
}
