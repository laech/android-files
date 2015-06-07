package l.files.ui.browser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import l.files.fs.Resource;
import l.files.ui.FileLabels;

import static android.R.id.icon;
import static android.R.id.title;
import static l.files.R.layout.files_activity_title;
import static l.files.R.layout.files_activity_title_item;
import static l.files.ui.IconFonts.getDirectoryIcon;

final class HierarchyAdapter extends BaseAdapter
{
    private final List<Resource> hierarchy = new ArrayList<>();

    void set(final Resource directory)
    {
        hierarchy.clear();
        hierarchy.addAll(directory.hierarchy());
        notifyDataSetChanged();
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

        final Resource res = getItem(position);

        ((TextView) view.findViewById(icon)).setTypeface(
                getDirectoryIcon(parent.getContext().getAssets(), res));

        ((TextView) view.findViewById(title)).setText(
                res.isRoot() ? res.path() : res.name());

        return view;
    }

    private View inflate(final int layout, final ViewGroup parent)
    {
        return LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
    }
}
