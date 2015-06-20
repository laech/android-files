package l.files.ui.browser;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l.files.R;
import l.files.common.app.OptionsMenus;
import l.files.common.widget.MultiChoiceModeListeners;
import l.files.fs.Resource;
import l.files.provider.bookmarks.BookmarkManagerImpl;
import l.files.ui.Animations;
import l.files.ui.BaseFileListFragment;
import l.files.ui.OpenFileRequest;
import l.files.ui.Preferences;
import l.files.ui.browser.FileListItem.File;
import l.files.ui.browser.FilesLoader.Result;
import l.files.ui.menu.BookmarkMenu;
import l.files.ui.menu.PasteMenu;
import l.files.ui.menu.ShowHiddenFilesMenu;
import l.files.ui.menu.SortMenu;
import l.files.ui.mode.CopyAction;
import l.files.ui.mode.CountSelectedItemsAction;
import l.files.ui.mode.CutAction;
import l.files.ui.mode.DeleteAction;
import l.files.ui.mode.SelectAllAction;
import l.files.ui.newdir.NewDirMenu;
import l.files.ui.rename.RenameAction;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static l.files.common.app.SystemServices.getClipboardManager;
import static l.files.ui.IOExceptions.message;
import static l.files.ui.Preferences.getShowHiddenFiles;
import static l.files.ui.Preferences.getSort;
import static l.files.ui.Preferences.isShowHiddenFilesKey;
import static l.files.ui.Preferences.isSortKey;

public final class FilesFragment extends BaseFileListFragment
        implements LoaderCallbacks<Result>, OnSharedPreferenceChangeListener
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

    public FilesFragment()
    {
        super(R.layout.files_fragment);
    }

    public Resource directory()
    {
        return directory;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        directory = getArguments().getParcelable(ARG_DIRECTORY);
        progress = (ProgressBar) getView().findViewById(android.R.id.progress);

        setupListView();
        setupOptionsMenu();
        setHasOptionsMenu(true);
        setListAdapter(FilesAdapter.get(getActivity()));

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

    @Override
    public void onListItemClick(
            final ListView list,
            final View view,
            final int pos,
            final long id)
    {
        super.onListItemClick(list, view, pos, id);
        final File item = (File)
                list.getItemAtPosition(pos);
        getBus().post(OpenFileRequest.create(item.resource()));
    }

    @Override
    public FilesAdapter getListAdapter()
    {
        return (FilesAdapter) super.getListAdapter();
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

    private void setupListView()
    {
        final Activity context = getActivity();
        final ClipboardManager clipboard = getClipboardManager(context);
        final ListView list = getListView();
        list.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
        list.setMultiChoiceModeListener(MultiChoiceModeListeners.compose(
                new CountSelectedItemsAction(this),
                new SelectAllAction(list),
                new CutAction(clipboard, this),
                new CopyAction(clipboard, this),
                new DeleteAction(context, this),
                new RenameAction(context.getFragmentManager(), this)
        ));
    }

    @Override
    public Resource getCheckedItem()
    {
        final int position = getCheckedItemPosition();
        return ((File) getListView().getItemAtPosition(position)).resource();
    }

    @Override
    public List<Resource> getCheckedItems()
    {
        final List<Integer> positions = getCheckedItemPositions();
        final List<Resource> resources = new ArrayList<>(positions.size());
        for (final int position : positions)
        {
            final FileListItem item = (FileListItem) getListView()
                    .getItemAtPosition(position);
            if (item.isFile())
            {
                resources.add(((File) item).resource());
            }
        }
        return resources;
    }

    @Override
    public Loader<Result> onCreateLoader(final int id, final Bundle bundle)
    {
        final Activity context = getActivity();
        final FileSort sort = getSort(context);
        final boolean showHidden = Preferences.getShowHiddenFiles(context);
        return new FilesLoader(context, directory, sort, showHidden);
    }

    @Override
    public void onLoadFinished(final Loader<Result> loader, final Result data)
    {
        if (getActivity() != null && !getActivity().isFinishing())
        {
            if (!getListAdapter().isEmpty() && isResumed())
            {
                Animations.animatePreDataSetChange(getListView());
            }
            getListAdapter().setItems(data.items());
            if (data.exception() != null)
            {
                overrideEmptyText(message(data.exception()));
            }
            else
            {
                overrideEmptyText(R.string.empty);
            }

            handler.removeCallbacks(checkProgress);
            progress.setVisibility(GONE);
        }
    }

    private void overrideEmptyText(final int resId)
    {
        final View root = getView();
        if (root != null)
        {
            ((TextView) root.findViewById(android.R.id.empty)).setText(resId);
        }
    }

    private void overrideEmptyText(final String text)
    {
        final View root = getView();
        if (root != null)
        {
            ((TextView) root.findViewById(android.R.id.empty)).setText(text);
        }
    }

    @Override
    public void onLoaderReset(final Loader<Result> loader)
    {
        getListAdapter().setItems(Collections.<FileListItem>emptyList());
    }

    @Override
    public void onSharedPreferenceChanged(
            final SharedPreferences pref,
            final String key)
    {

        if (isShowHiddenFilesKey(key))
        {
            filesLoader().setShowHidden(getShowHiddenFiles(getActivity()));
        }
        else if (isSortKey(key))
        {
            filesLoader().setSort(getSort(getActivity()));
        }
    }

    private FilesLoader filesLoader()
    {
        final Loader<?> _loader = getLoaderManager().getLoader(0);
        return (FilesLoader) _loader;
    }
}
