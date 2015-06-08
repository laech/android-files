package l.files.ui.browser;

import android.content.res.AssetManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import java.util.Objects;

import l.files.fs.Resource;
import l.files.ui.FileLabels;

import static android.R.id.icon;
import static android.R.id.title;
import static l.files.R.layout.files_activity_title;
import static l.files.R.layout.files_activity_title_item;
import static l.files.ui.IconFonts.getDirectoryIcon;

final class HierarchyAdapter extends BaseAdapter
{
    private ImmutableList<Resource> hierarchy = ImmutableList.of();
    private Resource directory;

    void set(final Resource dir)
    {
        directory = dir;
        hierarchy = ImmutableList.copyOf(dir.hierarchy()).reverse();
        notifyDataSetChanged();
    }

    ImmutableList<Resource> get()
    {
        return hierarchy;
    }

    int indexOf(final Resource dir)
    {
        return hierarchy.indexOf(dir);
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override
    public boolean isEnabled(final int position)
    {
        return !Objects.equals(directory, getItem(position));
    }

    @Override
    public int getCount()
    {
        return hierarchy.size();
    }

    @Override
    public Resource getItem(final int position)
    {
        return hierarchy.get(position);
    }

    @Override
    public long getItemId(final int position)
    {
        return position;
    }

    @Override
    public View getView(
            final int position,
            final View convertView,
            final ViewGroup parent)
    {
        final View view = convertView != null
                ? convertView
                : inflate(files_activity_title, parent);

        ((TextView) view.findViewById(title)).setText(
                FileLabels.get(parent.getResources(), getItem(position)));

        return view;
    }

    @Override
    public View getDropDownView(
            final int position,
            final View convertView,
            final ViewGroup parent)
    {
        final View view = convertView != null
                ? convertView
                : inflate(files_activity_title_item, parent);

        final boolean enabled = isEnabled(position);
        final Resource res = getItem(position);
        view.setEnabled(enabled);

        final AssetManager assets = parent.getContext().getAssets();
        final TextView iconView = (TextView) view.findViewById(icon);
        iconView.setTypeface(getDirectoryIcon(assets, res));
        iconView.setEnabled(enabled);

        final TextView titleView = (TextView) view.findViewById(title);
        titleView.setText(res.isRoot() ? res.path() : res.name());
        titleView.setEnabled(enabled);

        return view;
    }

    private View inflate(final int layout, final ViewGroup parent)
    {
        return LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
    }
}
