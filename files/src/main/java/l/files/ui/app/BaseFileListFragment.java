package l.files.ui.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.squareup.otto.Bus;
import l.files.event.OpenFileRequest;

import java.io.File;

import static l.files.event.Events.bus;

// TODO
public class BaseFileListFragment extends BaseListFragment {

  private Bus bus = bus();
  private final int layoutResourceId;

  public BaseFileListFragment(int layoutResourceId) {
    this.layoutResourceId = layoutResourceId;
  }

  public Bus getBus() {
    return bus;
  }

  public void setBus(Bus bus) {
    this.bus = bus;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override
  public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
    return inflater.inflate(layoutResourceId, container, false);
  }

  @Override
  public final void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    Object item = l.getItemAtPosition(pos);
    if (item instanceof File) bus.post(new OpenFileRequest((File) item));
  }

}
