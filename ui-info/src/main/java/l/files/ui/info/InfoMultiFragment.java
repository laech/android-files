package l.files.ui.info;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collection;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.ui.base.fs.FileInfo;

import static l.files.base.Objects.requireNonNull;

public final class InfoMultiFragment extends InfoBaseFragment {

    public static InfoMultiFragment create(
            Path parentDirectory,
            Collection<FileInfo> children
    ) {
        requireNonNull(parentDirectory);
        requireNonNull(children);
        return newFragment(newArgs(parentDirectory, children));
    }

    private static Bundle newArgs(
            Path parentDirectory,
            Collection<FileInfo> items
    ) {
        ArrayList<Name> names = new ArrayList<>(items.size());
        for (FileInfo item : items) {
            names.add(item.selfPath().name()); // TODO handle null
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_PARENT_DIRECTORY, parentDirectory);
        bundle.putParcelableArrayList(ARG_CHILDREN, names);
        return bundle;
    }

    private static InfoMultiFragment newFragment(Bundle bundle) {
        InfoMultiFragment fragment = new InfoMultiFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    int layoutResourceId() {
        return R.layout.info_multi_fragment;
    }

}
