package l.files.app;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.base.Optional;
import l.files.R;
import l.files.common.app.OptionsMenus;
import l.files.app.os.Monitor;
import l.files.common.widget.MultiChoiceModeListeners;

import java.io.File;
import java.util.List;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static java.util.Collections.emptyList;
import static l.files.app.menu.Menus.*;
import static l.files.app.mode.Modes.*;

public final class FilesFragment extends BaseFileListFragment implements Monitor.Callback {

    // TODO if fragment is paused, refresh on resume for changes?

    public static final String TAG = FilesFragment.class.getSimpleName();
    public static final String ARG_DIRECTORY = "directory";

    private Monitor mMonitor;
    private File mDirectory;
    private boolean mAnimateListChange;

    public FilesFragment() {
        super(R.layout.files_fragment);
    }

    public static FilesFragment create(File dir) {
        return Fragments.setArgs(new FilesFragment(), ARG_DIRECTORY, dir.getAbsolutePath());
    }

    public File getDirectory() {
        return mDirectory;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDirectory = new File(getArguments().getString(ARG_DIRECTORY));
        mMonitor = FilesApp.getMonitor(this);
        mAnimateListChange = false;

        setupListView();
        setupOptionsMenu();
        setListAdapter(FilesAdapter.get(getActivity()));
    }

    @Override
    public void onStart() {
        super.onStart();
        mMonitor.register(this, mDirectory);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMonitor.unregister(this, mDirectory);
        mAnimateListChange = false;
    }

    @Override
    public FilesAdapter getListAdapter() {
        return (FilesAdapter) super.getListAdapter();
    }

    @Override
    public void onRefreshed(Optional<? extends List<?>> content) {
        if (!content.isPresent()) {
            updateUnableToShowDirectoryError(mDirectory);
        } else {
            setContent(content.get(), mAnimateListChange);
        }
        mAnimateListChange = true;
    }

    @Override
    public String toString() {
        return FilesFragment.class.getSimpleName() + '@' + Integer.toHexString(hashCode());
    }

    private void setupOptionsMenu() {
        FragmentManager manager = getActivityFragmentManager();
        setOptionsMenu(OptionsMenus.compose(
                newBookmarkMenu(getBus(), mDirectory),
                newDirMenu(manager, mDirectory),
                newPasteMenu(getBus(), mDirectory),
                newSortMenu(manager),
                newShowHiddenFilesMenu(getBus())
        ));
    }

    private void setupListView() {
        ListView list = getListView();
        list.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
        FragmentManager manager = getActivityFragmentManager();
        list.setMultiChoiceModeListener(MultiChoiceModeListeners.compose(
                newCountSelectedItemsAction(list),
                newSelectAllAction(list),
                newCutAction(list, getBus()),
                newCopyAction(list, getBus()),
                newDeleteAction(list, getBus()),
                newRenameAction(list, manager)));
    }

    private FragmentManager getActivityFragmentManager() {
        return getActivity().getSupportFragmentManager();
    }

    private void updateUnableToShowDirectoryError(File directory) {
        if (!directory.exists()) {
            overrideEmptyText(R.string.directory_doesnt_exist);
        } else if (!directory.isDirectory()) {
            overrideEmptyText(R.string.not_a_directory);
        } else {
            overrideEmptyText(R.string.permission_denied);
        }
    }

    private void overrideEmptyText(int resId) {
        View root = getView();
        if (root != null) {
            ((TextView) root.findViewById(android.R.id.empty)).setText(resId);
        }
    }

    private void setContent(List<?> result, boolean animate) {
        if (result.size() == 0) {
            setEmptyContent();
        } else {
            getListAdapter().replace(getListView(), result, animate);
        }
    }

    private void setEmptyContent() {
        overrideEmptyText(R.string.empty);
        getListAdapter().replace(getListView(), emptyList(), false);
    }
}
