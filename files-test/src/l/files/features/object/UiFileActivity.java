package l.files.features.object;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static l.files.common.widget.ListViews.getItems;
import static l.files.features.object.Instrumentations.awaitOnMainThread;

import android.app.Instrumentation;
import android.view.ActionMode;
import android.view.MenuItem;
import android.widget.ListView;
import java.io.File;
import java.util.concurrent.Callable;
import l.files.R;
import l.files.app.FilesActivity;

public final class UiFileActivity {

  private final Instrumentation in;
  private final FilesActivity activity;

  public UiFileActivity(Instrumentation in, FilesActivity activity) {
    this.in = in;
    this.activity = activity;
  }

  public UiFileActivity check(final File file, final boolean checked) {
    awaitOnMainThread(in, new Callable<Boolean>() {
      @Override public Boolean call() {
        ListView list = list();
        int i = getItems(list).indexOf(file);
        if (i > -1) {
          list.setItemChecked(i, checked);
          return true;
        }
        return false;
      }
    });
    return this;
  }

  public UiRename rename() {
    awaitOnMainThread(in, new Runnable() {
      @Override public void run() {
        ActionMode mode = activity.getCurrentActionMode();
        ActionMode.Callback callback = activity.getCurrentActionModeCallback();
        assertTrue(callback.onActionItemClicked(mode, getRenameMenuItem()));
      }
    });
    return new UiRename(in, activity);
  }

  public UiFileActivity assertCanRename(final boolean can) {
    awaitOnMainThread(in, new Runnable() {
      @Override public void run() {
        assertEquals(can, getRenameMenuItem().isEnabled());
      }
    });
    return this;
  }

  private MenuItem getRenameMenuItem() {
    return activity.getCurrentActionMode().getMenu().findItem(R.id.rename);
  }

  private ListView list() {
    return (ListView) activity
        .findViewById(R.id.file_list_fragment)
        .findViewById(android.R.id.list);
  }
}
