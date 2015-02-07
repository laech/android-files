package l.files.ui.sidebar;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import l.files.R;
import l.files.fs.Path;
import l.files.ui.FileLabels;
import l.files.ui.StableFilesAdapter;

import static l.files.ui.IconFonts.getDirectoryIcon;

final class SidebarAdapter extends StableFilesAdapter<Path> {

  @Override public View getView(int position, View view, ViewGroup parent) {
    if (view == null) {
      view = inflate(R.layout.sidebar_item, parent);
      view.setTag(new ViewHolder(view));
    }

    AssetManager assets = parent.getContext().getAssets();
    Resources res = parent.getResources();

    Path path = getItem(position);
    ViewHolder holder = (ViewHolder) view.getTag();
    holder.setTitle(FileLabels.get(res, path));
    holder.setIcon(getDirectoryIcon(assets, path));

    return view;
  }

  @Override protected Object getItemIdObject(int position) {
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