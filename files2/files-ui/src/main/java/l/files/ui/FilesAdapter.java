package l.files.ui;

import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public final class FilesAdapter extends BaseAdapter {

  private Cursor cursor;

  public void setCursor(Cursor cursor) {
    this.cursor = cursor;
    notifyDataSetChanged();
  }

  @Override public int getCount() {
    return cursor == null ? 0 : 1;
  }

  @Override public Cursor getItem(int position) {
    cursor.moveToPosition(position);
    return cursor;
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    return null;
  }
}
