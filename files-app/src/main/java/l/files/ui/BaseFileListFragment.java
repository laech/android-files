package l.files.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.squareup.otto.Bus;

import l.files.common.app.BaseListFragment;

class BaseFileListFragment extends BaseListFragment {

  private Bus bus;
  private final int layoutResourceId;

  public BaseFileListFragment(int layoutResourceId) {
    this.layoutResourceId = layoutResourceId;
  }

  // TODO Review bus getter/setter is appropriate or not, possible leaks
  // when bus is changed after an object is registered?

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

  @Override
  public View onCreateView(
      @SuppressWarnings("NullableProblems") LayoutInflater inflater,
      ViewGroup container,
      Bundle state) {
    return inflater.inflate(layoutResourceId, container, false);
  }

  @Override
  public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    Cursor cursor = (Cursor) l.getItemAtPosition(pos);
    bus.post(OpenFileRequest.from(cursor));
  }
}
