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
import java.util.Objects;

import l.files.fs.File;
import l.files.ui.FileLabels;
import l.files.ui.Icons;

import static android.R.id.icon;
import static android.R.id.title;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static l.files.R.layout.files_activity_title;
import static l.files.R.layout.files_activity_title_item;

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
        return !Objects.equals(directory, getItem(position));
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
                : inflate(files_activity_title, parent);

        ((TextView) view.findViewById(title)).setText(
                FileLabels.get(parent.getResources(), getItem(position)));

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView != null
                ? convertView
                : inflate(files_activity_title_item, parent);

        boolean enabled = isEnabled(position);
        File res = getItem(position);
        view.setEnabled(enabled);

        AssetManager assets = parent.getContext().getAssets();
        TextView iconView = (TextView) view.findViewById(icon);
        iconView.setText(Icons.directoryIconStringId(res));
        iconView.setTypeface(Icons.font(assets));
        iconView.setEnabled(enabled);

        TextView titleView = (TextView) view.findViewById(title);
        titleView.setText(res.isRoot() ? res.path() : res.name());
        titleView.setEnabled(enabled);

        return view;
    }

    private View inflate(int layout, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
    }
}
