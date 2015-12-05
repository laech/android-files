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
import java.util.Collections;
import java.util.List;

import l.files.fs.Path;
import l.files.ui.base.app.OptionsMenus;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.base.selection.SelectionModeFragment;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.view.ActionModes;
import l.files.ui.base.view.ClearSelectionOnDestroyActionMode;
import l.files.ui.base.view.CountSelectedItemsAction;
import l.files.ui.bookmarks.actions.Bookmark;
import l.files.ui.browser.BrowserItem.FileItem;
import l.files.ui.browser.FilesLoader.Result;
import l.files.ui.operations.actions.Copy;
import l.files.ui.operations.actions.Cut;
import l.files.ui.operations.actions.Delete;
import l.files.ui.operations.actions.Paste;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
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
        SelectionModeFragment<Path, FileItem>

        implements
        LoaderCallbacks<Result>,
        OnSharedPreferenceChangeListener,
        Selectable {

    public static final String TAG = FilesFragment.class.getSimpleName();

    private static final String ARG_DIRECTORY = "directory";

    public static FilesFragment create(Path directory) {
        Bundle bundle = new Bundle(1);
        bundle.putParcelable(ARG_DIRECTORY, directory);

        FilesFragment browser = new FilesFragment();
        browser.setArguments(bundle);
        return browser;
    }

    private Path directory;
    private FilesAdapter adapter;

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
                ProgressBar progressBar = inflateProgressBar();
                progressBar.setProgress(current);
                progressBar.setIndeterminate(current == 0);
                progressBar.setMax(loader.approximateChildTotal());
                progressBar.setVisibility(VISIBLE);
            }
            handler.postDelayed(this, 10);

        }
    };

    public Path directory() {
        return directory;
    }

    public List<BrowserItem> items() {
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
        adapter = new FilesAdapter(
                getActivity(),
                selection(),
                actionModeProvider(),
                actionModeCallback(),
                (OnOpenFileListener) getActivity());

        int spanCount = getResources().getInteger(R.integer.files_grid_columns);
        recycler = find(android.R.id.list, this);
        recycler.setAdapter(adapter);
        recycler.setHasFixedSize(true);
        recycler.setItemViewCacheSize(spanCount * 3);
        recycler.setItemAnimator(null);
        recycler.setLayoutManager(new StaggeredGridLayoutManager(spanCount, VERTICAL));
        recycler.getRecycledViewPool().setMaxRecycledViews(VIEW_TYPE_FILE, 50);
        recycler.getRecycledViewPool().setMaxRecycledViews(VIEW_TYPE_HEADER, 50);
        recycler.addOnScrollListener(onScrollListener());

        setupOptionsMenu();
        setHasOptionsMenu(true);

        handler.postDelayed(checkProgress, 1000);
        getLoaderManager().initLoader(0, null, this);
        Preferences.register(getActivity(), this);
    }

    private RecyclerView.OnScrollListener onScrollListener() {
        return new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    adapter.warmUpOnIdle(getLayoutManager());
                }
            }

        };
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

    private Provider<Boolean> autoRefreshDisable() {
        return new Provider<Boolean>() {
            @Override
            public Boolean get() {
                FilesLoader loader = filesLoader();
                return loader != null && loader.autoRefreshDisabled();
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
        Activity context = getActivity();
        return new FilesLoader(
                context,
                directory,
                getSort(context),
                getShowHiddenFiles(context)
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

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.warmUpOnIdle(getLayoutManager());
                    }
                });
            }

        }
    }

    private StaggeredGridLayoutManager getLayoutManager() {
        return (StaggeredGridLayoutManager) recycler.getLayoutManager();
    }

    private void updateSelection(Result data) {
        List<Path> files = new ArrayList<>(data.items().size());
        for (BrowserItem item : data.items()) {
            if (item.isFileItem()) {
                files.add(((FileItem) item).selfPath());
            }
        }
        selection().retainAll(files);
    }

    @Override
    public void onLoaderReset(Loader<Result> loader) {
        adapter.setItems(Collections.<BrowserItem>emptyList());
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
