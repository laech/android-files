package l.files.ui.browser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.ui.base.fs.FileIcons;
import l.files.ui.base.view.Views;

public final class InfoMultiFragment extends InfoBaseFragment {

    public static InfoMultiFragment create(Path dir, Collection<FileItem> items) {

        ArrayList<Name> names = new ArrayList<>(items.size());
        for (FileItem item : items) {
            names.add(item.selfPath().name());
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_DIR, dir);
        bundle.putParcelableArrayList(ARG_CHILDREN, names);

        InfoMultiFragment fragment = new InfoMultiFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.info_multi_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Views.<TextView>find(android.R.id.icon, this)
                .setTypeface(FileIcons.font(getContext().getAssets()));

        initLoader();
    }

}
