package l.files.ui.browser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;

import l.files.fs.File;
import l.files.fs.Name;
import l.files.ui.browser.BrowserItem.FileItem;

public final class InfoMultiFragment extends InfoBaseFragment {

    public static InfoMultiFragment create(File dir, Collection<FileItem> items) {

        ArrayList<Name> names = new ArrayList<>(items.size());
        for (FileItem item : items) {
            names.add(item.selfFile().name());
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
        initLoader();
    }

}
