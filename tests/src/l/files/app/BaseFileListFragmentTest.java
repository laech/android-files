package l.files.app;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.squareup.otto.Bus;
import l.files.app.BaseFileListFragment;
import l.files.event.OpenFileRequest;

import java.io.File;

import static org.mockito.Mockito.*;

public abstract class BaseFileListFragmentTest<T extends Activity>
    extends ActivityInstrumentationTestCase2<T> {

  public BaseFileListFragmentTest(Class<T> activityClass) {
    super(activityClass);
  }

  @UiThreadTest public void testBusIsRegisteredOnResume() {
    fragment().setBus(mock(Bus.class));
    fragment().onResume();
    verify(fragment().getBus()).register(fragment());
  }

  @UiThreadTest public void testBusIsUnregisteredOnPause() {
    fragment().setBus(mock(Bus.class));
    fragment().onResume();
    fragment().onPause();
    verify(fragment().getBus()).unregister(fragment());
  }

  @UiThreadTest public void testBusIsNotifiedOnFileSelection() {
    final File file = new File("/");
    fragment().setBus(mock(Bus.class));
    fragment().onListItemClick(newListView(file), null, 0, 0);
    verify(fragment().getBus()).post(new OpenFileRequest(file));
  }

  @UiThreadTest public void testBusIsNotNotifiedOnNonFileSelection() {
    fragment().setBus(mock(Bus.class));
    fragment().onListItemClick(newListView("hello"), null, 0, 0);
    verifyZeroInteractions(fragment().getBus());
  }

  private ListView newListView(Object... items) {
    ListView list = new ListView(getActivity());
    list.setAdapter(newAdapter(items));
    return list;
  }

  private ArrayAdapter<Object> newAdapter(Object... items) {
    return new ArrayAdapter<Object>(getActivity(), 0, items);
  }

  protected abstract BaseFileListFragment fragment();

}
