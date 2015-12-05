package l.files.ui.browser;

import android.content.res.AssetManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l.files.fs.File;
import l.files.fs.Name;
import l.files.ui.base.fs.FileIcons;
import l.files.ui.base.fs.FileLabels;

import static android.R.id.icon;
import static android.R.id.title;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static l.files.ui.base.fs.UserDirs.DIR_HOME;

final class HierarchyAdapter extends BaseAdapter {
    private List<File> hierarchy = emptyList();
    private File directory;

    void set(File dir) {
        directory = dir;
        hierarchy = new ArrayList<>(dir.hierarchy());
        Collections.reverse(hierarchy);
        hierarchy = unmodifiableList(hierarchy);
        notifyDataSetChanged();
    }

    List<File> get() {
        return hierarchy;
    }

    int indexOf(File dir) {
        return hierarchy.indexOf(dir);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return !directory.equals(getItem(position));
    }

    @Override
    public int getCount() {
        return hierarchy.size();
    }

    @Override
    public File getItem(int position) {
        return hierarchy.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView != null
                ? convertView
                : inflate(R.layout.files_activity_title, parent);

        File file = getItem(position);

        TextView title = (TextView) view.findViewById(android.R.id.title);
        title.setText(FileLabels.get(parent.getResources(), file));

        TextView icon = (TextView) view.findViewById(android.R.id.icon);
        icon.setTypeface(FileIcons.font(view.getContext().getAssets()));
        icon.setText(FileIcons.directoryIconStringId(file));

        if (file.equals(DIR_HOME)) {
            title.setVisibility(GONE);
            icon.setVisibility(VISIBLE);
        } else {
            title.setVisibility(VISIBLE);
            icon.setVisibility(GONE);
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView != null
                ? convertView
                : inflate(R.layout.files_activity_title_item, parent);

        boolean enabled = isEnabled(position);
        File res = getItem(position);
        view.setEnabled(enabled);

        AssetManager assets = parent.getContext().getAssets();
        TextView iconView = (TextView) view.findViewById(icon);
        iconView.setText(FileIcons.directoryIconStringId(res));
        iconView.setTypeface(FileIcons.font(assets));
        iconView.setEnabled(enabled);

        TextView titleView = (TextView) view.findViewById(title);
        Name name = res.name();
        titleView.setText(!name.isEmpty() ? name.toString() : res.pathString());
        titleView.setEnabled(enabled);

        return view;
    }

    private View inflate(int layout, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
    }
}
