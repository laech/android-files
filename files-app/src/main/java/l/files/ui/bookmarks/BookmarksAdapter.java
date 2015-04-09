package l.files.ui.bookmarks;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import l.files.R;
import l.files.fs.Resource;
import l.files.ui.FileLabels;
import l.files.ui.StableAdapter;

import static l.files.ui.IconFonts.getDirectoryIcon;

final class BookmarksAdapter extends StableAdapter<Resource> {

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = inflate(R.layout.bookmark_item, parent);
            view.setTag(new ViewHolder(view));
        }

        AssetManager assets = parent.getContext().getAssets();
        Resources res = parent.getResources();

        Resource resource = getItem(position);
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.setTitle(FileLabels.get(res, resource.getPath()));
        holder.setIcon(getDirectoryIcon(assets, resource.getResource()));

        return view;
    }

    @Override
    protected Object getItemIdObject(int position) {
        return getItem(position);
    }

    private static final class ViewHolder {
        final TextView title;
        final TextView icon;

        private ViewHolder(View root) {
            this.title = (TextView) root.findViewById(android.R.id.title);
            this.icon = (TextView) root.findViewById(android.R.id.icon);
        }

        void setTitle(CharSequence text) {
            title.setText(text);
        }

        void setIcon(Typeface typeface) {
            icon.setTypeface(typeface);
        }
    }
}
