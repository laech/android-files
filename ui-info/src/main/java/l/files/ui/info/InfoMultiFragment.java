package l.files.ui.info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.ui.base.fs.FileInfo;

public final class InfoMultiFragment extends InfoBaseFragment {

    public static InfoMultiFragment create(Path dir, Collection<FileInfo> items) {

        ArrayList<Name> names = new ArrayList<>(items.size());
        for (FileInfo item : items) {
            names.add(item.selfPath().name()); // TODO handle null
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
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.info_multi_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initLoader();
    }

}
