package l.files.app;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import l.files.R;

import static l.files.provider.FileCursors.getFileId;
import static l.files.provider.FileCursors.getFileName;

final class SidebarAdapter extends StableFilesAdapter {

  @Override public View getView(int position, View view, ViewGroup parent) {
    if (view == null) {
      view = inflate(R.layout.sidebar_item, parent);
      view.setTag(new ViewHolder(view));
    }

    AssetManager assets = parent.getContext().getAssets();
    Resources res = parent.getResources();

    Cursor cursor = getItem(position);
    String id = getFileId(cursor);
    String name = getFileName(cursor);

    ViewHolder holder = (ViewHolder) view.getTag();
    holder.setTitle(FileLabels.get(res, id, name));
    holder.setIcon(IconFonts.forDirectoryId(assets, id));

    return view;
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
