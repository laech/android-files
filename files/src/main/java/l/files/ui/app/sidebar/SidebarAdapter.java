package l.files.ui.app.sidebar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.google.common.base.Function;
import l.files.R;

import java.io.File;

public class SidebarAdapter extends ArrayAdapter<Object> {

  private final Function<File, Drawable> drawables;
  private final Function<File, String> labels;

  SidebarAdapter(
      Context context,
      Function<File, Drawable> drawables,
      Function<File, String> labels) {
    super(context, 0);
    this.drawables = drawables;
    this.labels = labels;
  }

  @Override public boolean isEnabled(int position) {
    return getItem(position) instanceof File;
  }

  @Override public int getItemViewType(int position) {
    return getItem(position) instanceof File ? 0 : 1;
  }

  @Override public int getViewTypeCount() {
    return 2;
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    Object item = getItem(position);
    return item instanceof File
        ? getViewForFile((File) item, view, parent)
        : getViewForHeader(item, view, parent);
  }

  private View getViewForFile(File file, View view, ViewGroup parent) {
    if (view == null) view = inflate(getItemTextViewResourceId(), parent);
    updateViewForFile(file, (TextView) view);
    return view;
  }

  protected int getItemTextViewResourceId() {
    return R.layout.files_item;
  }

  private View getViewForHeader(Object header, View view, ViewGroup parent) {
    if (view == null) view = inflate(getHeaderTextViewResourceId(), parent);
    updateViewForHeader(header, (TextView) view);
    return view;
  }

  protected int getHeaderTextViewResourceId() {
    return R.layout.sidebar_item_header;
  }

  void updateViewForFile(File file, TextView view) {
    view.setEnabled(file.canRead());
    view.setText(labels.apply(file));
    view.setCompoundDrawablesWithIntrinsicBounds(drawables.apply(file), null, null, null);
  }

  void updateViewForHeader(Object header, TextView view) {
    view.setText(header.toString());
  }

  private View inflate(int viewId, ViewGroup parent) {
    Context context = parent.getContext();
    return LayoutInflater.from(context).inflate(viewId, parent, false);
  }
}
