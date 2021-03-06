package l.files.ui.browser;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import l.files.base.lifecycle.CollectionLiveData;
import l.files.ui.base.app.OptionsMenus;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.fs.OpenFileEvent;
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
import l.files.ui.browser.preference.DefaultPreferencesViewModel;
import l.files.ui.info.action.InfoAction;
import l.files.ui.operations.action.CopyAction;
import l.files.ui.operations.action.CutAction;
import l.files.ui.operations.action.DeleteAction;
import l.files.ui.operations.menu.PasteMenu;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL;
import static java.util.Collections.emptyList;
import static l.files.bookmarks.BookmarksKt.getBookmarks;
import static l.files.ui.base.fs.IOExceptions.message;
import static l.files.ui.browser.FilesAdapter.VIEW_TYPE_FILE;
import static l.files.ui.browser.FilesAdapter.VIEW_TYPE_HEADER;
import static l.files.ui.browser.preference.DefaultPreferencesViewModelKt.getDefaultPreferencesViewModel;
import static l.files.ui.browser.preference.Preferences.*;

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

    public static FilesFragment create(Path directory) {
        Bundle bundle = new Bundle(2);
        bundle.putString(ARG_DIRECTORY, directory.toString());

        FilesFragment browser = new FilesFragment();
        browser.setArguments(bundle);
        return browser;
    }

    @Nullable
    private Path directory;

    private FilesAdapter adapter;

    private CollectionLiveData<Path, Set<Path>, Set<Path>> bookmarks;

    private DefaultPreferencesViewModel preferencesModel;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        postponeEnterTransition();
    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle state
    ) {
        return inflater.inflate(R.layout.files_fragment, container, false);
    }

    @Override
    public void onViewCreated(
        @NonNull View view,
        @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);
        preferencesModel = getDefaultPreferencesViewModel(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        assert args != null;
        directory = Paths.get(args.getString(ARG_DIRECTORY));

        bookmarks = getBookmarks(this);

        View view = getView();
        assert view != null;
        int spanCount = getResources().getInteger(R.integer.files_grid_columns);
        recycler = view.findViewById(android.R.id.list);
        recycler.setHasFixedSize(true);
        recycler.setItemViewCacheSize(spanCount * 3);
        recycler.setLayoutManager(new StaggeredGridLayoutManager(
            spanCount,
            VERTICAL
        ));
        recycler.getRecycledViewPool().setMaxRecycledViews(VIEW_TYPE_FILE, 50);
        recycler.getRecycledViewPool()
            .setMaxRecycledViews(VIEW_TYPE_HEADER, 50);
        recycler.setAdapter(adapter = new FilesAdapter(
            recycler,
            this,
            selection(),
            actionModeProvider(),
            actionModeCallback(),
            OpenFileEvent.topic
        ));

        setupOptionsMenu();
        setHasOptionsMenu(true);

        handler.postDelayed(checkProgress, 1000);

        if (hasReadExternalStoragePermission()) {
            initLoad();
        } else {
            requestPermissions(
                PERM_EXTERNAL_STORAGE,
                PERM_REQ_EXTERNAL_STORAGE_INIT
            );
        }

        preferencesModel.getPreferences()
            .observe(
                getViewLifecycleOwner(),
                pref -> pref.registerOnSharedPreferenceChangeListener(this)
            );
    }

    private boolean hasReadExternalStoragePermission() {
        Activity activity = getActivity();
        assert activity != null;
        int state = checkSelfPermission(activity, READ_EXTERNAL_STORAGE);
        return state == PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        @NonNull String[] permissions,
        @NonNull int[] grantResults
    ) {

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
            ViewStub stub = view.findViewById(R.id.progress_stub);
            progressBar = stub.inflate().findViewById(android.R.id.progress);
        }
        return progressBar;
    }

    @Nullable
    private TextView emptyView;

    private TextView inflateEmptyView() {
        if (emptyView == null) {
            View view = getView();
            assert view != null;
            ViewStub stub = view.findViewById(R.id.empty_stub);
            emptyView = stub.inflate().findViewById(android.R.id.empty);
        }
        return emptyView;
    }

    private void setupOptionsMenu() {
        FilesActivity activity = (FilesActivity) getActivity();
        assert activity != null;
        FragmentManager manager = activity.getSupportFragmentManager();
        setOptionsMenu(OptionsMenus.compose(
            new RefreshMenu(() -> refreshEnabled, this::refresh),
            new BookmarkMenu(directory(), bookmarks),
            new NewDirMenu(manager, directory()),
            new PasteMenu(activity, directory()),
            new SortMenu(manager),
            new ShowHiddenFilesMenu(activity)
        ));
    }

    private void refresh() {
        if (hasReadExternalStoragePermission()) {
            restartLoad();
        } else {
            requestPermissions(
                PERM_EXTERNAL_STORAGE,
                PERM_REQ_EXTERNAL_STORAGE_REFRESH
            );
        }
    }

    private boolean refreshEnabled;

    @Override
    public void selectAll() {
        assert adapter != null;
        adapter.selectAll();
    }

    @Override
    protected ActionMode.Callback actionModeCallback() {
        FilesActivity activity = (FilesActivity) getActivity();
        assert activity != null;
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
            new ShareAction(selection(), activity)
        );
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
            () -> getSort(context),
            () -> getShowHiddenFiles(context)
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

            adapter.setItems(data.items());

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

        if (isShowHiddenFilesKey(key) || isSortKey(key)) {
            loader.updateAll();
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
