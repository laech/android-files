package l.files.ui.browser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.annotation.Nullable;

import l.files.fs.Path;
import l.files.ui.base.fs.FileIcons;
import l.files.ui.base.fs.FileLabels;

import static android.graphics.Color.WHITE;
import static android.graphics.PorterDuff.Mode.SRC_ATOP;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static l.files.ui.base.fs.UserDirs.DIR_HOME;

final class HierarchyAdapter extends BaseAdapter {

    private List<Path> hierarchy = emptyList();

    @Nullable
    private Path directory;

    void set(Path dir) {
        directory = dir;
        hierarchy = new ArrayList<>(dir.hierarchy());
        Collections.reverse(hierarchy);
        hierarchy = unmodifiableList(hierarchy);
        notifyDataSetChanged();
    }

    List<Path> get() {
        return hierarchy;
    }

    int indexOf(Path dir) {
        return hierarchy.indexOf(dir);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return isEnabled(getItem(position));
    }

    private boolean isEnabled(Path path) {
        assert directory != null;
        return !directory.equals(path);
    }

    @Override
    public int getCount() {
        return hierarchy.size();
    }

    @Override
    public Path getItem(int position) {
        return hierarchy.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        View view = convertView != null
                ? convertView
                : inflate(R.layout.files_activity_title, parent);

        Path path = getItem(position);

        TextView title = view.findViewById(R.id.title);
        title.setText(FileLabels.get(parent.getResources(), path));

        ImageView icon = view.findViewById(R.id.icon);
        icon.setImageResource(FileIcons.getDirectory(path));
        icon.setColorFilter(WHITE, SRC_ATOP);

        if (path.equals(DIR_HOME)) {
            title.setVisibility(GONE);
            icon.setVisibility(VISIBLE);
        } else {
            title.setVisibility(VISIBLE);
            icon.setVisibility(GONE);
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, ViewGroup parent) {
        View view = convertView != null
                ? convertView
                : inflate(R.layout.files_activity_title_item, parent);

        boolean enabled = isEnabled(position);
        Path path = getItem(position);
        view.setEnabled(enabled);

        ImageView iconView = view.findViewById(R.id.icon);
        iconView.setImageResource(FileIcons.getDirectory(path));
        iconView.setEnabled(enabled);
        iconView.setAlpha(enabled ? 0.54f : 0.2f);

        TextView titleView = view.findViewById(R.id.title);
        titleView.setText(String.valueOf(path.getName().orObject(path)));
        titleView.setEnabled(enabled);

        return view;
    }

    private View inflate(int layout, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
    }
}
