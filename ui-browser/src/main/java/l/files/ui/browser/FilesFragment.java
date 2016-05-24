package l.files.ui.browser;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import l.files.base.Provider;
import l.files.fs.Path;
import l.files.ui.base.app.OptionsMenus;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.base.selection.SelectionModeFragment;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.view.ActionModes;
import l.files.ui.base.view.ClearSelectionOnDestroyActionMode;
import l.files.ui.base.view.CountSelectedItemsAction;
import l.files.ui.bookmarks.actions.Bookmark;
import l.files.ui.browser.FilesLoader.Result;
import l.files.ui.info.actions.Info;
import l.files.ui.operations.actions.Copy;
import l.files.ui.operations.actions.Cut;
import l.files.ui.operations.actions.Delete;
import l.files.ui.operations.actions.Paste;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Collections.emptyList;
import static l.files.ui.base.fs.IOExceptions.message;
import static l.files.ui.base.view.Views.find;
import static l.files.ui.browser.FilesAdapter.VIEW_TYPE_FILE;
import static l.files.ui.browser.FilesAdapter.VIEW_TYPE_HEADER;
import static l.files.ui.browser.Preferences.getShowHiddenFiles;
import static l.files.ui.browser.Preferences.getSort;
import static l.files.ui.browser.Preferences.isShowHiddenFilesKey;
import static l.files.ui.browser.Preferences.isSortKey;

public final class FilesFragment

        extends
        SelectionModeFragment<Path, FileInfo>

        implements
        LoaderCallbacks<Result>,
        OnSharedPreferenceChangeListener,
        Selectable {

    public static final String TAG = FilesFragment.class.getSimpleName();

    private static final String ARG_DIRECTORY = "directory";
    private static final String ARG_WATCH_LIMIT = "watch_limit";

    public static FilesFragment create(Path directory, int watchLimit) {
        Bundle bundle = new Bundle(2);
        bundle.putParcelable(ARG_DIRECTORY, directory);
        bundle.putInt(ARG_WATCH_LIMIT, watchLimit);

        FilesFragment browser = new FilesFragment();
        browser.setArguments(bundle);
        return browser;
    }

    private Path directory;
    private int watchLimit;
    private FilesAdapter adapter;

    public RecyclerView recycler;

    private final Handler handler = new Handler();

    private final Runnable checkProgress = new Runnable() {
        @Override
        public void run() {

            Activity activity = getActivity();
            if (activity == null
                    || activity.isFinishing()
                    || isRemoving()
                    || isDetached()) {
                return;
            }

            FilesLoader loader = filesLoader();
            if (loader != null) {
                int current = loader.approximateChildLoaded();
                ProgressBar progressBar = inflateProgressBar();
                /*
                 * Note: do not collapse this if-else to:
                 *
                 * progressBar.setProgress(current);
                 * progressBar.setIndeterminate(current == 0);
                 * progressBar.setMax(loader.approximateChildTotal());
                 *
                 * because that will cause the indeterminate progress
                 * bar to be blank on Android 4.3.
                 */
                if (current > 0) {
                    progressBar.setProgress(current);
                    progressBar.setMax(loader.approximateChildTotal());
                    progressBar.setIndeterminate(false);
                } else {
                    progressBar.setIndeterminate(true);
                }
                progressBar.setVisibility(VISIBLE);
            }
            handler.postDelayed(this, 10);

        }
    };

    public Path directory() {
        return directory;
    }

    public List<Object> items() {
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
        watchLimit = getArguments().getInt(ARG_WATCH_LIMIT, -1);

        int spanCount = getResources().getInteger(R.integer.files_grid_columns);
        recycler = find(android.R.id.list, this);
        recycler.setHasFixedSize(true);
        recycler.setItemViewCacheSize(spanCount * 3);
        recycler.setItemAnimator(null);
        recycler.setLayoutManager(new StaggeredGridLayoutManager(spanCount, VERTICAL));
        recycler.getRecycledViewPool().setMaxRecycledViews(VIEW_TYPE_FILE, 50);
        recycler.getRecycledViewPool().setMaxRecycledViews(VIEW_TYPE_HEADER, 50);
        recycler.setAdapter(adapter = new FilesAdapter(
                recycler,
                selection(),
                actionModeProvider(),
                actionModeCallback(),
                (OnOpenFileListener) getActivity()));

        setupOptionsMenu();
        setHasOptionsMenu(true);

        handler.postDelayed(checkProgress, 1000);
        getLoaderManager().initLoader(0, null, this);
        Preferences.register(getActivity(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Preferences.unregister(getActivity(), this);
    }

    private ProgressBar progressBar;

    private ProgressBar inflateProgressBar() {
        if (progressBar == null) {
            //noinspection ConstantConditions
            ViewStub stub = (ViewStub) getView().findViewById(R.id.progress_stub);
            progressBar = (ProgressBar) stub.inflate().findViewById(android.R.id.progress);
        }
        return progressBar;
    }

    private TextView emptyView;

    private TextView inflateEmptyView() {
        if (emptyView == null) {
            //noinspection ConstantConditions
            ViewStub stub = (ViewStub) getView().findViewById(R.id.empty_stub);
            emptyView = (TextView) stub.inflate().findViewById(android.R.id.empty);
        }
        return emptyView;
    }

    private void setupOptionsMenu() {
        FragmentActivity context = getActivity();
        FragmentManager manager = context.getSupportFragmentManager();
        setOptionsMenu(OptionsMenus.compose(
                new Refresh(autoRefreshDisable(), refresh()),
                new Bookmark(directory, context),
                new NewDirMenu(manager, directory),
                new Paste(context, directory),
                new SortMenu(manager),
                new ShowHiddenFilesMenu(context)
        ));
    }

    private Runnable refresh() {
        return new Runnable() {
            @Override
            public void run() {
                getLoaderManager().restartLoader(0, null, FilesFragment.this);
            }
        };
    }

    private boolean refreshEnabled;

    private Provider<Boolean> autoRefreshDisable() {
        return new Provider<Boolean>() {
            @Override
            public Boolean get() {
                return refreshEnabled;
            }
        };
    }

    @Override
    public void selectAll() {
        adapter.selectAll();
    }

    @Override
    protected ActionMode.Callback actionModeCallback() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        return ActionModes.compose(
                new CountSelectedItemsAction(selection()),
                new ClearSelectionOnDestroyActionMode(selection()),
                new Info(selection(), manager, directory()),
                new SelectAllAction(this),
                new Cut(selection()),
                new Copy(selection()),
                new Delete(selection(), manager),
                new RenameAction(selection(), manager),
                new Share(selection(), getActivity())
        );
    }

    @Override
    protected ActionModeProvider actionModeProvider() {
        return (ActionModeProvider) getActivity();
    }

    @Override
    public Loader<Result> onCreateLoader(int id, Bundle bundle) {
        refreshEnabled = false;
        Activity context = getActivity();
        return new FilesLoader(
                context,
                directory,
                getSort(context),
                getShowHiddenFiles(context),
                watchLimit
        );
    }

    @Override
    public void onLoadFinished(Loader<Result> loader, Result data) {
        handler.removeCallbacks(checkProgress);

        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {

            if (progressBar != null) {
                progressBar.setVisibility(GONE);
            }

            // First load no animation to speed up
            if (recycler.getItemAnimator() == null && adapter.getItemCount() != 0) {
                recycler.setItemAnimator(new DefaultItemAnimator());
            }

            updateSelection(data);
            adapter.setItems(data.items());
            //noinspection ThrowableResultOfMethodCallIgnored
            if (data.exception() != null) {
                inflateEmptyView().setText(message(data.exception()));
                inflateEmptyView().setVisibility(VISIBLE);

            } else if (adapter.isEmpty()) {
                inflateEmptyView().setText(R.string.empty);
                inflateEmptyView().setVisibility(VISIBLE);

            } else {
                if (emptyView != null) {
                    emptyView.setVisibility(GONE);
                }
            }

        }

        refreshEnabled = true;
    }

    private void updateSelection(Result data) {
        List<Path> files = new ArrayList<>(data.items().size());
        for (Object item : data.items()) {
            if (item instanceof FileInfo) {
                files.add(((FileInfo) item).selfPath());
            }
        }
        selection().retainAll(files);
    }

    @Override
    public void onLoaderReset(Loader<Result> loader) {
        adapter.setItems(emptyList());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        FilesLoader loader = filesLoader();
        if (loader == null) {
            return;
        }

        if (isShowHiddenFilesKey(key)) {
            loader.setShowHidden(getShowHiddenFiles(activity));
        } else if (isSortKey(key)) {
            loader.setSort(getSort(activity));
        }
    }

    private FilesLoader filesLoader() {
        try {
            Loader<?> _loader = getLoaderManager().getLoader(0);
            return (FilesLoader) _loader;
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
