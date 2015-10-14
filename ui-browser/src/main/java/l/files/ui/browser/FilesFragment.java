package l.files.ui.browser;

import android.app.Activity;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import l.files.fs.File;
import l.files.ui.Preferences;
import l.files.ui.R;
import l.files.ui.base.app.OptionsMenus;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.base.selection.SelectionModeFragment;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.view.ActionModes;
import l.files.ui.base.view.ClearSelectionOnDestroyActionMode;
import l.files.ui.base.view.CountSelectedItemsAction;
import l.files.ui.bookmarks.actions.Bookmark;
import l.files.ui.browser.FilesLoader.Result;
import l.files.ui.menu.ShowHiddenFilesMenu;
import l.files.ui.menu.SortMenu;
import l.files.ui.mode.SelectAllAction;
import l.files.ui.mode.Selectable;
import l.files.ui.newdir.NewDirMenu;
import l.files.ui.operations.actions.Copy;
import l.files.ui.operations.actions.Cut;
import l.files.ui.operations.actions.Delete;
import l.files.ui.operations.actions.Paste;
import l.files.ui.rename.RenameAction;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static l.files.ui.Preferences.getShowHiddenFiles;
import static l.files.ui.Preferences.getSort;
import static l.files.ui.Preferences.isShowHiddenFilesKey;
import static l.files.ui.Preferences.isSortKey;
import static l.files.ui.R.integer.files_grid_columns;
import static l.files.ui.base.fs.IOExceptions.message;
import static l.files.ui.base.view.Views.find;

public final class FilesFragment extends SelectionModeFragment<File>
        implements
        LoaderCallbacks<Result>,
        OnSharedPreferenceChangeListener,
        Selectable {

    public static final String TAG = FilesFragment.class.getSimpleName();

    private static final String ARG_DIRECTORY = "directory";

    public static FilesFragment create(File directory) {
        Bundle bundle = new Bundle(1);
        bundle.putParcelable(ARG_DIRECTORY, directory);

        FilesFragment browser = new FilesFragment();
        browser.setArguments(bundle);
        return browser;
    }

    private File directory;
    private ProgressBar progress;
    private FilesAdapter adapter;
    private TextView empty;

    public RecyclerView recycler;

    private final Handler handler = new Handler();
    private final Runnable checkProgress = new Runnable() {
        @Override
        public void run() {
            Activity activity = getActivity();
            if (activity == null
                    || activity.isFinishing()
                    || activity.isDestroyed()
                    || isRemoving()
                    || isDetached()) {
                return;
            }

            FilesLoader loader = filesLoader();
            if (loader != null) {
                int current = loader.approximateChildLoaded();
                progress.setProgress(current);
                progress.setIndeterminate(current == 0);
                progress.setMax(loader.approximateChildTotal());
                progress.setVisibility(VISIBLE);
            }
            handler.postDelayed(this, 10);
        }
    };

    public File directory() {
        return directory;
    }

    public List<FileListItem> items() {
        return adapter.items();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle state) {
        return inflater.inflate(R.layout.files_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        directory = getArguments().getParcelable(ARG_DIRECTORY);
        empty = find(android.R.id.empty, this);
        progress = find(android.R.id.progress, this);
        adapter = new FilesAdapter(
                getActivity(),
                selection(),
                actionModeProvider(),
                actionModeCallback(),
                (OnOpenFileListener) getActivity());

        int columns = getResources().getInteger(files_grid_columns);
        recycler = find(android.R.id.list, this);
        recycler.setAdapter(adapter);
        recycler.setHasFixedSize(true);
        recycler.setItemViewCacheSize(columns * 3);
        recycler.setLayoutManager(new StaggeredGridLayoutManager(columns, VERTICAL));

        setupOptionsMenu();
        setHasOptionsMenu(true);

        handler.postDelayed(checkProgress, 500);
        getLoaderManager().initLoader(0, null, this);
        Preferences.register(getActivity(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Preferences.unregister(getActivity(), this);
    }

    private void setupOptionsMenu() {
        Activity context = getActivity();
        setOptionsMenu(OptionsMenus.compose(
                new Bookmark(directory, context),
                new NewDirMenu(context.getFragmentManager(), directory),
                new Paste(context, directory),
                new SortMenu(context.getFragmentManager()),
                new ShowHiddenFilesMenu(context)
        ));
    }

    @Override
    public void selectAll() {
        adapter.selectAll();
    }

    @Override
    protected ActionMode.Callback actionModeCallback() {
        Activity context = getActivity();
        return ActionModes.compose(
                new CountSelectedItemsAction(selection()),
                new ClearSelectionOnDestroyActionMode(selection()),
                new SelectAllAction(this),
                new Cut(selection(), context),
                new Copy(selection(), context),
                new Delete(selection(), context),
                new RenameAction(selection(), context.getFragmentManager())
        );
    }

    @Override
    protected ActionModeProvider actionModeProvider() {
        return (ActionModeProvider) getActivity();
    }

    @Override
    public Loader<Result> onCreateLoader(int id, Bundle bundle) {
        Activity context = getActivity();
        FileSort sort = getSort(context);
        Collator collator = Collator.getInstance(Locale.getDefault());
        boolean showHidden = Preferences.getShowHiddenFiles(context);
        return new FilesLoader(context, directory, sort, collator, showHidden);
    }

    @Override
    public void onLoadFinished(Loader<Result> loader, Result data) {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            adapter.setItems(data.items());
            //noinspection ThrowableResultOfMethodCallIgnored
            if (data.exception() != null) {
                empty.setText(message(data.exception()));
            } else {
                empty.setText(R.string.empty);
            }

            handler.removeCallbacks(checkProgress);
            progress.setVisibility(GONE);

            if (adapter.isEmpty()) {
                empty.setVisibility(VISIBLE);
            } else {
                empty.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Result> loader) {
        adapter.setItems(Collections.<FileListItem>emptyList());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (isShowHiddenFilesKey(key)) {
            filesLoader().setShowHidden(getShowHiddenFiles(activity));
        } else if (isSortKey(key)) {
            filesLoader().setSort(getSort(activity));
        }
    }

    private FilesLoader filesLoader() {
        Loader<?> _loader = getLoaderManager().getLoader(0);
        return (FilesLoader) _loader;
    }
}
