package l.files.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.squareup.otto.Bus;
import java.io.File;
import l.files.common.app.BaseListFragment;

class BaseFileListFragment extends BaseListFragment {

  private Bus bus;
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

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bus = FilesApp.getBus(this);
  }

  @Override public void onStart() {
    super.onStart();
    bus.register(this);
  }

  @Override public void onStop() {
    super.onStop();
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
    if (item instanceof File) {
      bus.post(new OpenFileRequest((File) item));
    }
  }
}
