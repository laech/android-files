package l.files.event;

import static android.content.ClipData.newIntent;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.io.Files.toAbsolutePaths;
import static l.files.common.io.Files.toFiles;

import android.content.ClipboardManager;
import android.content.Intent;
import com.google.common.base.Supplier;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import java.io.File;
import java.util.Set;

final class ClipboardProvider implements Supplier<Clipboard> {

  static final String ACTION_CUT = "l.files.intent.action.CUT";
  static final String ACTION_COPY = "l.files.intent.action.COPY";
  static final String EXTRA_FILES = "l.files.intent.extra.FILES";

  private final ClipboardManager manager;

  ClipboardProvider(ClipboardManager manager) {
    this.manager = checkNotNull(manager, "manager");
  }

  @Produce @Override public Clipboard get() {
    try {

      Intent intent = manager.getPrimaryClip().getItemAt(0).getIntent();
      String action = intent.getAction();
      if (ACTION_CUT.equals(action)) return new Clipboard.Cut(getFiles(intent));
      if (ACTION_COPY.equals(action)) return new Clipboard.Copy(getFiles(intent));
      return null;

    } catch (NullPointerException e) {
      return null;
    } catch (IndexOutOfBoundsException e) {
      return null;
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @Subscribe public void handle(CutRequest request) {
    setClipData(ACTION_CUT, request.value());
  }

  @Subscribe public void handle(CopyRequest request) {
    setClipData(ACTION_COPY, request.value());
  }

  private void setClipData(String action, Set<File> files) {
    String[] paths = toAbsolutePaths(files.toArray(new File[files.size()]));
    manager.setPrimaryClip(newIntent(null,
        new Intent(action).putExtra(EXTRA_FILES, paths)));
  }

  public static File[] getFiles(Intent intent) {
    return toFiles(intent.getStringArrayExtra(EXTRA_FILES));
  }
}
