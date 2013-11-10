package l.files.app;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.net.URI;

import l.files.R;
import l.files.common.app.BaseListFragment;
import l.files.common.app.OptionsMenus;
import l.files.common.widget.MultiChoiceModeListeners;

import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static l.files.app.mode.Modes.newCountSelectedItemsAction;
import static l.files.app.mode.Modes.newSelectAllAction;
import static l.files.provider.FilesContract.FileInfo.COLUMN_ID;
import static l.files.provider.FilesContract.buildFileChildrenUri;

public final class FilesFragment
    extends BaseListFragment implements LoaderCallbacks<Cursor> {

  public static final String TAG = FilesFragment.class.getSimpleName();
  public static final String ARG_DIRECTORY = "directory";

  private String mDirectory;

  public static FilesFragment create(String dir) {
    return Fragments.setArgs(new FilesFragment(), ARG_DIRECTORY, dir);
  }

  public String getDirectory() {
    return mDirectory;
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.files_fragment, container, false);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mDirectory = getArguments().getString(ARG_DIRECTORY);

    setupListView();
    setupOptionsMenu();
    setListAdapter(FilesAdapter.get(getActivity()));

    getLoaderManager().initLoader(0, null, this);
  }

  @Override public FilesAdapter getListAdapter() {
    return (FilesAdapter) super.getListAdapter();
  }

  private void setupOptionsMenu() {
//    FragmentManager manager = getActivityFragmentManager();
    setOptionsMenu(OptionsMenus.compose(
//        newBookmarkMenu(getBus(), mDirectory),
//        newDirMenu(manager, mDirectory),
//        newPasteMenu(getBus(), mDirectory),
//        newSortMenu(manager),
//        newShowHiddenFilesMenu(getBus())
    ));
  }

  private void setupListView() {
    ListView list = getListView();
    list.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
//    FragmentManager manager = getActivityFragmentManager();
    list.setMultiChoiceModeListener(MultiChoiceModeListeners.compose(
        newCountSelectedItemsAction(list),
        newSelectAllAction(list)
//        newCutAction(list, getBus()),
//        newCopyAction(list, getBus()),
//        newDeleteAction(list, getBus()),
//        newRenameAction(list, manager)
    ));
  }

//  private FragmentManager getActivityFragmentManager() {
//    return getActivity().getSupportFragmentManager();
//  }


  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    Cursor cursor = (Cursor) l.getItemAtPosition(position);
    String uri = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
    FilesApp.getBus(this).post(new OpenFileRequest(new File(URI.create(uri))));
    // TODO
  }

  @Override public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    return new CursorLoader(
        getActivity(),
        buildFileChildrenUri(mDirectory),
        null,
        null,
        null,
        null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    getListAdapter().setCursor(cursor);
    if (cursor.getCount() == 0) overrideEmptyText(R.string.empty);
  }

  private void overrideEmptyText(int resId) {
    View root = getView();
    if (root != null) {
      ((TextView) root.findViewById(android.R.id.empty)).setText(resId);
    }
  }

  @Override public void onLoaderReset(Loader<Cursor> loader) {
    getListAdapter().setCursor(null);
  }
}
