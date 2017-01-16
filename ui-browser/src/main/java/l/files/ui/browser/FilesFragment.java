package l.files.ui.browser;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import l.files.base.Provider;
import l.files.fs.Path;
import l.files.premium.ConsumeTestPurchasesOnDebugMenu;
import l.files.premium.PremiumLock;
import l.files.ui.base.app.OptionsMenus;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.selection.SelectionModeFragment;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.view.ActionModes;
import l.files.ui.base.view.ClearSelectionOnDestroyActionMode;
import l.files.ui.base.view.CountSelectedItemsAction;
import l.files.ui.bookmarks.menus.BookmarkMenu;
import l.files.ui.browser.FilesLoader.Result;
import l.files.ui.browser.action.RenameAction;
import l.files.ui.browser.action.SelectAllAction;
import l.files.ui.browser.action.Selectable;
import l.files.ui.browser.action.ShareAction;
import l.files.ui.browser.menu.NewDirMenu;
import l.files.ui.browser.menu.RefreshMenu;
import l.files.ui.browser.menu.ShowHiddenFilesMenu;
import l.files.ui.browser.menu.SortMenu;
import l.files.ui.browser.preference.Preferences;
import l.files.ui.info.action.InfoAction;
import l.files.ui.operations.action.CopyAction;
import l.files.ui.operations.action.CutAction;
import l.files.ui.operations.action.DeleteAction;
import l.files.ui.operations.menu.PasteMenu;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static l.files.premium.PremiumLock.isPremiumPreferenceKey;
import static l.files.ui.base.fs.IOExceptions.message;
import static l.files.ui.base.view.Views.find;
import static l.files.ui.browser.FilesAdapter.VIEW_TYPE_FILE;
import static l.files.ui.browser.FilesAdapter.VIEW_TYPE_HEADER;
import static l.files.ui.browser.preference.Preferences.getShowHiddenFiles;
import static l.files.ui.browser.preference.Preferences.getSort;
import static l.files.ui.browser.preference.Preferences.isShowHiddenFilesKey;
import static l.files.ui.browser.preference.Preferences.isSortKey;

public final class FilesFragment

        extends
        SelectionModeFragment<Path, FileInfo>

        implements
        LoaderCallbacks<Result>,
        OnSharedPreferenceChangeListener,
        Selectable {

    public static final String TAG = FilesFragment.class.getSimpleName();

    private static final int PERM_REQ_EXTERNAL_STORAGE_INIT = 1;
    private static final int PERM_REQ_EXTERNAL_STORAGE_REFRESH = 2;

    private static final String[] PERM_EXTERNAL_STORAGE =
            new String[]{WRITE_EXTERNAL_STORAGE};

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

    @Nullable
    private Path directory;
    private int watchLimit;

    @Nullable
    private FilesAdapter adapter;

    @Nullable
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
        assert directory != null;
        return directory;
    }

    public List<Object> items() {
        assert adapter != null;
        return adapter.items();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle state
    ) {
        return inflater.inflate(R.layout.files_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        directory = getArguments().getParcelable(ARG_DIRECTORY);
        watchLimit = getArguments().getInt(ARG_WATCH_LIMIT, -1);

        int spanCount = getResources().getInteger(R.integer.files_grid_columns);
        recycler = find(android.R.id.list, this);
        recycler.setHasFixedSize(true);
        recycler.setItemViewCacheSize(spanCount * 3);
        recycler.setLayoutManager(new StaggeredGridLayoutManager(spanCount, VERTICAL));
        recycler.getRecycledViewPool().setMaxRecycledViews(VIEW_TYPE_FILE, 50);
        recycler.getRecycledViewPool().setMaxRecycledViews(VIEW_TYPE_HEADER, 50);
        recycler.setAdapter(adapter = new FilesAdapter(
                recycler,
                this,
                selection(),
                actionModeProvider(),
                actionModeCallback(),
                (FilesActivity) getActivity(),
                ((FilesActivity) getActivity()).getPremiumLock()));

        setupOptionsMenu();
        setHasOptionsMenu(true);

        handler.postDelayed(checkProgress, 1000);

        if (hasPermission(READ_EXTERNAL_STORAGE)) {
            initLoad();
        } else {
            requestPermissions(
                    PERM_EXTERNAL_STORAGE,
                    PERM_REQ_EXTERNAL_STORAGE_INIT
            );
        }

        Preferences.register(getActivity(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Preferences.unregister(getActivity(), this);
    }

    private boolean hasPermission(String permission) {
        int state = checkSelfPermission(getActivity(), permission);
        return state == PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        // Not checking grant results, load error will show in view
        switch (requestCode) {

            case PERM_REQ_EXTERNAL_STORAGE_INIT:
                initLoad();
                break;

            case PERM_REQ_EXTERNAL_STORAGE_REFRESH:
                restartLoad();
                break;

        }
    }

    private void initLoad() {
        getLoaderManager().initLoader(0, null, this);
    }

    private void restartLoad() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Nullable
    private ProgressBar progressBar;

    private ProgressBar inflateProgressBar() {
        if (progressBar == null) {
            View view = getView();
            assert view != null;
            ViewStub stub = (ViewStub) view.findViewById(R.id.progress_stub);
            progressBar = (ProgressBar) stub.inflate().findViewById(android.R.id.progress);
        }
        return progressBar;
    }

    @Nullable
    private TextView emptyView;

    private TextView inflateEmptyView() {
        if (emptyView == null) {
            View view = getView();
            assert view != null;
            ViewStub stub = (ViewStub) view.findViewById(R.id.empty_stub);
            emptyView = (TextView) stub.inflate().findViewById(android.R.id.empty);
        }
        return emptyView;
    }

    private void setupOptionsMenu() {
        FilesActivity activity = (FilesActivity) getActivity();
        PremiumLock premiumLock = activity.getPremiumLock();
        FragmentManager manager = activity.getSupportFragmentManager();
        setOptionsMenu(OptionsMenus.compose(
                new RefreshMenu(autoRefreshDisable(), refresh()),
                new BookmarkMenu(directory, activity),
                new NewDirMenu(manager, directory),
                new PasteMenu(activity, directory),
                new SortMenu(manager),
                new ShowHiddenFilesMenu(activity),
                new ConsumeTestPurchasesOnDebugMenu(premiumLock)
        ));
    }

    private Runnable refresh() {
        return new Runnable() {
            @Override
            public void run() {
                if (hasPermission(READ_EXTERNAL_STORAGE)) {
                    restartLoad();
                } else {
                    requestPermissions(
                            PERM_EXTERNAL_STORAGE,
                            PERM_REQ_EXTERNAL_STORAGE_REFRESH
                    );
                }
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
        assert adapter != null;
        adapter.selectAll();
    }

    @Override
    protected ActionMode.Callback actionModeCallback() {
        FilesActivity activity = (FilesActivity) getActivity();
        FragmentManager manager = activity.getSupportFragmentManager();
        return ActionModes.compose(
                new CountSelectedItemsAction(selection()),
                new ClearSelectionOnDestroyActionMode(selection()),
                new InfoAction(selection(), manager, directory()),
                new SelectAllAction(this),
                new CutAction(selection()),
                new CopyAction(selection()),
                new DeleteAction(selection(), manager),
                new RenameAction(selection(), manager),
                new ShareAction(selection(), activity));
    }

    @Override
    protected ActionModeProvider actionModeProvider() {
        return (ActionModeProvider) getActivity();
    }

    @Override
    public Loader<Result> onCreateLoader(int id, Bundle bundle) {
        assert directory != null;
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

            updateSelection(data);

            // TODO make this cleaner
            List<Object> items;
            if (!((FilesActivity) getActivity()).getPremiumLock().isUnlocked()
                    && !data.items().isEmpty()) {
                items = new ArrayList<>();
                items.add(Ad.INSTANCE);
                items.addAll(data.items());
                items = unmodifiableList(items);
            } else {
                items = data.items();
            }
            assert adapter != null;
            adapter.setItems(items);

            IOException e = data.exception();
            if (e != null) {
                inflateEmptyView().setText(message(e));
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
        assert adapter != null;
        adapter.setItems(emptyList());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        FilesActivity activity = (FilesActivity) getActivity();
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

        } else if (isPremiumPreferenceKey(key)
                && activity.getPremiumLock().isUnlocked()) {
            assert adapter != null;
            adapter.removeAd();
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
