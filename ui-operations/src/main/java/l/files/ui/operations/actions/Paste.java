package l.files.ui.operations.actions;

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

import l.files.fs.File;
import l.files.operations.OperationService;
import l.files.ui.base.app.OptionsMenuAction;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.content.Context.CLIPBOARD_SERVICE;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.operations.actions.Clipboards.clear;
import static l.files.ui.operations.actions.Clipboards.getFiles;
import static l.files.ui.operations.actions.Clipboards.isCopy;
import static l.files.ui.operations.actions.Clipboards.isCut;

public final class Paste extends OptionsMenuAction
        implements LoaderCallbacks<Paste.FileExistence> {

    private final File destination;
    private final ClipboardManager manager;
    private final Activity context;

    private Menu menu;

    public Paste(Activity context, File destination) {
        super(android.R.id.paste);
        this.context = requireNonNull(context, "context");
        this.manager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        this.destination = requireNonNull(destination, "destination");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        menu.add(NONE, id(), NONE, android.R.string.paste)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        this.menu = menu;
        MenuItem item = menu.findItem(id());
        if (item == null) {
            return;
        }

        item.setEnabled(Clipboards.hasClip(manager));
        final Set<File> files = Clipboards.getFiles(manager);
        for (File file : files) {
            if (this.destination.pathStartsWith(file)) {
                // Can't paste into itself
                item.setEnabled(false);
                return;
            }
        }

        context.getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    protected void onItemSelected(MenuItem item) {
        if (isCopy(manager)) {
            OperationService.copy(context, getFiles(manager), destination);
        } else if (isCut(manager)) {
            OperationService.move(context, getFiles(manager), destination);
            clear(manager);
        }
    }

    @Override
    public Loader<FileExistence> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<FileExistence>(context) {
            @Override
            public FileExistence loadInBackground() {
                Set<File> files = Clipboards.getFiles(manager);
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
                        // Ignore this file
                    }
                }
                return new FileExistence(files, exists);
            }

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                forceLoad();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<FileExistence> loader, FileExistence data) {
        if (menu == null) {
            return;
        }
        MenuItem item = menu.findItem(id());
        if (item != null && data.originals.equals(getFiles(manager))) {
            item.setEnabled(!data.exists.isEmpty());
        }
    }

    @Override
    public void onLoaderReset(Loader<FileExistence> loader) {
    }

    static final class FileExistence {
        final Set<File> originals;
        final Set<File> exists;

        private FileExistence(Set<File> originals, Set<File> exists) {
            this.originals = originals;
            this.exists = exists;
        }
    }
}
