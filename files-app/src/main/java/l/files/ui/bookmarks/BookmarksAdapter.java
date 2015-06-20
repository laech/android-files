package l.files.ui.bookmarks;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import l.files.R;
import l.files.fs.Resource;
import l.files.ui.FileLabels;
import l.files.ui.Icons;
import l.files.ui.StableAdapter;

import static l.files.ui.Icons.directoryIconStringId;

final class BookmarksAdapter extends StableAdapter<Resource>
{

    @Override
    public View getView(final int position, View view, final ViewGroup parent)
    {
        if (view == null)
        {
            view = inflate(R.layout.bookmark_item, parent);
            view.setTag(new ViewHolder(view));
        }

        final Resource resource = getItem(position);
        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.set(resource);
        return view;
    }

    @Override
    protected Object getItemIdObject(final int position)
    {
        return getItem(position);
    }

    private static final class ViewHolder
    {
        final TextView title;
        final TextView icon;

        ViewHolder(final View root)
        {
            title = (TextView) root.findViewById(android.R.id.title);
            icon = (TextView) root.findViewById(android.R.id.icon);
            icon.setTypeface(Icons.font(root.getResources().getAssets()));
        }

        void set(final Resource resource)
        {
            title.setText(FileLabels.get(title.getResources(), resource));
            icon.setText(directoryIconStringId(resource));
        }
    }
}
