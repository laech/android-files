package l.files.ui.menu;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.ClipboardManager;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Set;

import l.files.common.app.OptionsMenuAction;
import l.files.fs.Path;
import l.files.operations.OperationService;
import l.files.ui.Clipboards;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static java.util.Objects.requireNonNull;
import static l.files.ui.Clipboards.clear;
import static l.files.ui.Clipboards.getPaths;
import static l.files.ui.Clipboards.isCopy;
import static l.files.ui.Clipboards.isCut;

public final class PasteMenu extends OptionsMenuAction
    implements LoaderCallbacks<PasteMenu.PathExistent> {

  private final Path path;
  private final ClipboardManager manager;
  private final Activity context;

  private Menu menu;

  public PasteMenu(Activity context, ClipboardManager manager, Path path) {
    super(android.R.id.paste);
    this.context = requireNonNull(context);
    this.manager = requireNonNull(manager);
    this.path = requireNonNull(path);
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    this.menu = menu;
    menu.add(NONE, id(), NONE, android.R.string.paste)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    this.menu = menu;
    MenuItem item = menu.findItem(id());
    if (item == null) {
      return;
    }

    item.setEnabled(Clipboards.hasClip(manager));
    final Set<Path> paths = Clipboards.getPaths(manager);
    for (Path p : paths) {
      if (this.path.startsWith(p)) {
        // Can't paste into itself
        item.setEnabled(false);
        return;
      }
    }

    context.getLoaderManager().restartLoader(0, null, this);
  }

  @Override protected void onItemSelected(MenuItem item) {
    if (isCopy(manager)) {
      OperationService.copy(context, getPaths(manager), path);
    } else if (isCut(manager)) {
      OperationService.move(context, getPaths(manager), path);
      clear(manager);
    }
  }

  @Override public Loader<PathExistent> onCreateLoader(int id, Bundle args) {
    return new AsyncTaskLoader<PathExistent>(context) {
      @Override public PathExistent loadInBackground() {
        Set<Path> paths = Clipboards.getPaths(manager);
        Set<Path> exists = newHashSetWithExpectedSize(paths.size());
        for (Path path : paths) {
          if (isLoadInBackgroundCanceled()) {
            return null;
          }
          if (path.getResource().exists()) {
            exists.add(path);
          }
        }
        return new PathExistent(paths, exists);
      }

      @Override protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
      }
    };
  }

  @Override public void onLoadFinished(Loader<PathExistent> loader, PathExistent data) {
    if (menu == null) {
      return;
    }
    MenuItem item = menu.findItem(id());
    if (item != null && data.originals.equals(getPaths(manager))) {
      item.setEnabled(!data.exists.isEmpty());
    }
  }

  @Override public void onLoaderReset(Loader<PathExistent> loader) {}

  static final class PathExistent {
    final Set<Path> originals;
    final Set<Path> exists;

    private PathExistent(Set<Path> originals, Set<Path> exists) {
      this.originals = originals;
      this.exists = exists;
    }
  }
}
