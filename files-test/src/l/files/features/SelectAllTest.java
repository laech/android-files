package l.files.features;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.common.widget.ListViews.getCheckedItems;
import static l.files.test.Tests.waitUntilSuccessful;

import android.widget.ListView;
import java.io.File;
import java.util.Set;
import l.files.test.BaseFilesActivityTest;

public final class SelectAllTest extends BaseFilesActivityTest {

  // TODO use feature.object

  public void testSelectsAll() throws Throwable {
    final Set<File> files = createFilesInDir(3);
    waitUntilLoaded();
    setItemChecked(0);
    clickSelectAllAction();
    assertAllChecked(files);
  }

  private void assertAllChecked(final Set<File> files) throws Throwable {
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        assertEquals(files, newHashSet(getCheckedItems(listView(), File.class)));
      }
    });
  }

  private ListView listView() {
    return (ListView) getActivity().findViewById(android.R.id.list);
  }

  private void waitUntilLoaded() {
    waitUntilSuccessful(new Runnable() {
      @Override public void run() {
        assertTrue(listView().getCount() > 0);
      }
    }, 1, SECONDS);
  }

  private void setItemChecked(final int position) throws Throwable {
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        listView().setItemChecked(position, true);
      }
    });
  }

  private void clickSelectAllAction() throws Throwable {
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getActivity().findViewById(android.R.id.selectAll).performClick();
      }
    });
  }

  private Set<File> createFilesInDir(int n) {
    Set<File> files = newHashSetWithExpectedSize(n);
    for (int i = 0; i < n; i++) {
      files.add(dir().newFile());
    }
    return files;
  }
}
