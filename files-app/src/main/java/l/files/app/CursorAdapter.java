package l.files.app;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

abstract class CursorAdapter extends BaseAdapter {

  private Cursor cursor;

  public void setCursor(Cursor cursor) {
    this.cursor = cursor;
    notifyDataSetChanged();
  }

  @Override public int getCount() {
    return cursor == null ? 0 : cursor.getCount();
  }

  @Override public Cursor getItem(int position) {
    cursor.moveToPosition(position);
    return cursor;
  }

  @Override public long getItemId(int position) {
    return position;
  }


  protected View inflate(int layout, ViewGroup parent) {
    return LayoutInflater.from(parent.getContext())
        .inflate(layout, parent, false);
  }
}
