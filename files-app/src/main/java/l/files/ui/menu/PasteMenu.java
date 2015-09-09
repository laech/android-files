package l.files.ui.menu;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.ClipboardManager;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import l.files.common.app.OptionsMenuAction;
import l.files.fs.File;
import l.files.operations.OperationService;
import l.files.ui.Clipboards;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.Clipboards.clear;
import static l.files.ui.Clipboards.getResources;
import static l.files.ui.Clipboards.isCopy;
import static l.files.ui.Clipboards.isCut;

public final class PasteMenu extends OptionsMenuAction
    implements LoaderCallbacks<PasteMenu.ResourceExistence> {

  private final File destination;
  private final ClipboardManager manager;
  private final Activity context;

  private Menu menu;

  public PasteMenu(
      Activity context,
      ClipboardManager manager,
      File destination) {
    super(android.R.id.paste);
    this.context = requireNonNull(context, "context");
    this.manager = requireNonNull(manager, "manager");
    this.destination = requireNonNull(destination, "destination");
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
    final Set<File> files = Clipboards.getResources(manager);
    for (File file : files) {
      if (this.destination.startsWith(file)) {
        // Can't paste into itself
        item.setEnabled(false);
        return;
      }
    }

    context.getLoaderManager().restartLoader(0, null, this);
  }

  @Override protected void onItemSelected(MenuItem item) {
    if (isCopy(manager)) {
      OperationService.copy(context, getResources(manager), destination);
    } else if (isCut(manager)) {
      OperationService.move(context, getResources(manager), destination);
      clear(manager);
    }
  }

  @Override
  public Loader<ResourceExistence> onCreateLoader(int id, Bundle args) {
    return new AsyncTaskLoader<ResourceExistence>(context) {
      @Override public ResourceExistence loadInBackground() {
        Set<File> files = Clipboards.getResources(manager);
        Set<File> exists = new HashSet<>();
        for (File file : files) {
          if (isLoadInBackgroundCanceled()) {
            return null;
          }
          try {
            if (file.exists(NOFOLLOW)) {
              exists.add(file);
            }
          } catch (IOException e) {
            // Ignore this resource
          }
        }
        return new ResourceExistence(files, exists);
      }

      @Override protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
      }
    };
  }

  @Override
  public void onLoadFinished(Loader<ResourceExistence> loader, ResourceExistence data) {
    if (menu == null) {
      return;
    }
    MenuItem item = menu.findItem(id());
    if (item != null && data.originals.equals(getResources(manager))) {
      item.setEnabled(!data.exists.isEmpty());
    }
  }

  @Override public void onLoaderReset(Loader<ResourceExistence> loader) {
  }

  static final class ResourceExistence {
    final Set<File> originals;
    final Set<File> exists;

    private ResourceExistence(Set<File> originals, Set<File> exists) {
      this.originals = originals;
      this.exists = exists;
    }
  }
}
