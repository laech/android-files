package l.files.ui.browser;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.VisibleForTesting;
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

import l.files.R;
import l.files.common.app.OptionsMenus;
import l.files.common.view.ActionModeProvider;
import l.files.common.widget.ActionModes;
import l.files.fs.Resource;
import l.files.operations.Events;
import l.files.provider.bookmarks.BookmarkManagerImpl;
import l.files.ui.Preferences;
import l.files.ui.browser.FilesLoader.Result;
import l.files.ui.menu.BookmarkMenu;
import l.files.ui.menu.PasteMenu;
import l.files.ui.menu.ShowHiddenFilesMenu;
import l.files.ui.menu.SortMenu;
import l.files.ui.mode.ClearSelectionOnDestroyActionMode;
import l.files.ui.mode.CopyAction;
import l.files.ui.mode.CountSelectedItemsAction;
import l.files.ui.mode.CutAction;
import l.files.ui.mode.DeleteAction;
import l.files.ui.mode.SelectAllAction;
import l.files.ui.mode.Selectable;
import l.files.ui.newdir.NewDirMenu;
import l.files.ui.rename.RenameAction;
import l.files.ui.selection.SelectionModeFragment;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static l.files.R.integer.files_list_columns;
import static l.files.common.app.SystemServices.getClipboardManager;
import static l.files.common.view.Views.find;
import static l.files.ui.IOExceptions.message;
import static l.files.ui.Preferences.getShowHiddenFiles;
import static l.files.ui.Preferences.getSort;
import static l.files.ui.Preferences.isShowHiddenFilesKey;
import static l.files.ui.Preferences.isSortKey;

public final class FilesFragment extends SelectionModeFragment<Resource>
        implements
        LoaderCallbacks<Result>,
        OnSharedPreferenceChangeListener,
        Selectable
{
    public static final String TAG = FilesFragment.class.getSimpleName();

    private static final String ARG_DIRECTORY = "directory";

    public static FilesFragment create(final Resource directory)
    {
        final Bundle bundle = new Bundle(1);
        bundle.putParcelable(ARG_DIRECTORY, directory);

        final FilesFragment browser = new FilesFragment();
        browser.setArguments(bundle);
        return browser;
    }

    private Resource directory;
    private ProgressBar progress;
    private FilesAdapter adapter;
    private TextView empty;

    @VisibleForTesting
    public RecyclerView recycler;


    private final Handler handler = new Handler();
    private final Runnable checkProgress = new Runnable()
    {
        @Override
        public void run()
        {
            final Activity activity = getActivity();
            if (activity == null
                    || activity.isFinishing()
                    || activity.isDestroyed()
                    || isRemoving()
                    || isDetached())
            {
                return;
            }

            final FilesLoader loader = filesLoader();
            if (loader != null)
            {
                final int current = loader.approximateChildLoaded();
                progress.setProgress(current);
                progress.setIndeterminate(current == 0);
                progress.setMax(loader.approximateChildTotal());
                progress.setVisibility(VISIBLE);
            }
            handler.postDelayed(this, 10);
        }
    };

    public Resource directory()
    {
        return directory;
    }

    public List<FileListItem> items()
    {
        return adapter.items();
    }

    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle state)
    {
        return inflater.inflate(R.layout.files_fragment, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        directory = getArguments().getParcelable(ARG_DIRECTORY);
        empty = find(android.R.id.empty, this);
        progress = find(android.R.id.progress, this);
        adapter = new FilesAdapter(
                getActivity(),
                selection(),
                actionModeProvider(),
                actionModeCallback(),
                Events.get());

        recycler = find(android.R.id.list, this);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new StaggeredGridLayoutManager(
                getResources().getInteger(files_list_columns), VERTICAL));

        setupOptionsMenu();
        setHasOptionsMenu(true);

        handler.postDelayed(checkProgress, 500);
        getLoaderManager().initLoader(0, null, this);
        Preferences.register(getActivity(), this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Preferences.unregister(getActivity(), this);
    }

    private void setupOptionsMenu()
    {
        final Activity context = getActivity();
        setOptionsMenu(OptionsMenus.compose(
                new BookmarkMenu(BookmarkManagerImpl.get(context), directory),
                new NewDirMenu(context.getFragmentManager(), directory),
                new PasteMenu(context, getClipboardManager(context), directory),
                new SortMenu(context.getFragmentManager()),
                new ShowHiddenFilesMenu(context)
        ));
    }

    @Override
    public void selectAll()
    {
        adapter.selectAll();
    }

    @Override
    protected ActionMode.Callback actionModeCallback()
    {
        final Activity context = getActivity();
        final ClipboardManager clipboard = getClipboardManager(context);
        return ActionModes.compose(
                new CountSelectedItemsAction(selection()),
                new ClearSelectionOnDestroyActionMode(selection()),
                new SelectAllAction(this),
                new CutAction(clipboard, selection()),
                new CopyAction(clipboard, selection()),
                new DeleteAction(context, selection()),
                new RenameAction(context.getFragmentManager(), selection())
        );
    }

    @Override
    protected ActionModeProvider actionModeProvider()
    {
        return (ActionModeProvider) getActivity();
    }

    @Override
    public Loader<Result> onCreateLoader(final int id, final Bundle bundle)
    {
        final Activity context = getActivity();
        final FileSort sort = getSort(context);
        final Collator collator = Collator.getInstance(Locale.getDefault());
        final boolean showHidden = Preferences.getShowHiddenFiles(context);
        return new FilesLoader(context, directory, sort, collator, showHidden);
    }

    @Override
    public void onLoadFinished(final Loader<Result> loader, final Result data)
    {
        final Activity activity = getActivity();
        if (activity != null && !activity.isFinishing())
        {
            adapter.setItems(data.items());
            //noinspection ThrowableResultOfMethodCallIgnored
            if (data.exception() != null)
            {
                empty.setText(message(data.exception()));
            }
            else
            {
                empty.setText(R.string.empty);
            }

            handler.removeCallbacks(checkProgress);
            progress.setVisibility(GONE);

            if (adapter.isEmpty())
            {
                empty.setVisibility(VISIBLE);
            }
            else
            {
                empty.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(final Loader<Result> loader)
    {
        adapter.setItems(Collections.<FileListItem>emptyList());
    }

    @Override
    public void onSharedPreferenceChanged(
            final SharedPreferences pref,
            final String key)
    {
        final Activity activity = getActivity();
        if (activity == null)
        {
            return;
        }

        if (isShowHiddenFilesKey(key))
        {
            filesLoader().setShowHidden(getShowHiddenFiles(activity));
        }
        else if (isSortKey(key))
        {
            filesLoader().setSort(getSort(activity));
        }
    }

    private FilesLoader filesLoader()
    {
        final Loader<?> _loader = getLoaderManager().getLoader(0);
        return (FilesLoader) _loader;
    }
}
