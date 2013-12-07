package l.files.app;

import android.animation.LayoutTransition;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import l.files.R;

import static android.animation.LayoutTransition.TransitionListener;
import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static android.view.View.FOCUS_RIGHT;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_ID;
import static l.files.provider.FilesContract.FileInfo.COLUMN_MEDIA_TYPE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_NAME;
import static l.files.provider.FilesContract.FileInfo.COLUMN_READABLE;
import static l.files.provider.FilesContract.FileInfo.MEDIA_TYPE_DIR;
import static l.files.provider.FilesContract.buildHierarchyUri;

public final class PathBarFragment extends Fragment
    implements LoaderCallbacks<Cursor>, OnClickListener, TransitionListener {

  private String fileId;
  private HorizontalScrollView scroller;
  private ViewGroup container;

  public void set(String fileId) {
    this.fileId = fileId;
    if (getActivity() != null) {
      getLoaderManager().restartLoader(0, null, this);
    }
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.path_bar, container, false);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    View root = getView();
    scroller = (HorizontalScrollView) root.findViewById(R.id.path_bar_scroll);
    container = (ViewGroup) root.findViewById(R.id.path_item_container);
    container.getLayoutTransition().addTransitionListener(this);
    if (fileId != null) {
      getLoaderManager().restartLoader(0, null, this);
    }
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Uri uri = buildHierarchyUri(fileId);
    return new CursorLoader(getActivity(), uri, null, null, null, null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    updatePathBar(cursor);
  }

  private void updatePathBar(Cursor cursor) {
    if (cursor.moveToFirst()) {
      int columnId = cursor.getColumnIndex(COLUMN_ID);
      int columnName = cursor.getColumnIndex(COLUMN_NAME);
      int columnMime = cursor.getColumnIndex(COLUMN_MEDIA_TYPE);
      int columnReadable = cursor.getColumnIndex(COLUMN_READABLE);
      LayoutInflater inflater = LayoutInflater.from(getActivity());

      do {
        View view = container.getChildAt(cursor.getPosition());
        if (view == null) {
          view = inflater.inflate(R.layout.path_bar_item, container, false);
          container.addView(view);
        }
        view.setTag(R.id.file_id, cursor.getString(columnId));
        view.setTag(R.id.file_name, cursor.getString(columnName));
        view.setTag(R.id.is_readable, cursor.getInt(columnReadable) == 1);
        view.setTag(R.id.is_directory, cursor.getString(columnMime).equals(MEDIA_TYPE_DIR));
        view.setVisibility(VISIBLE);
        view.setOnClickListener(this);

        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(cursor.isFirst() ? Build.MODEL : cursor.getString(columnName));

        TextView icon = (TextView) view.findViewById(R.id.icon);
        icon.setTypeface(IconFonts.forDirectoryId(getActivity().getAssets(), cursor.getString(columnId)));

      } while (cursor.moveToNext());

      for (int i = cursor.getPosition(); i < container.getChildCount(); i++) {
        container.getChildAt(i).setVisibility(GONE);
      }
    }
  }

  @Override public void onLoaderReset(Loader<Cursor> loader) {}

  @Override public void onClick(View v) {
    FilesApp.getBus(this).post(new OpenFileRequest(
        (String) v.getTag(R.id.file_id),
        (String) v.getTag(R.id.file_name),
        (boolean) v.getTag(R.id.is_readable),
        (boolean) v.getTag(R.id.is_directory)));
  }

  @Override public void startTransition(
      LayoutTransition transition, ViewGroup container, View view, int type) {
  }

  @Override public void endTransition(
      LayoutTransition transition, ViewGroup container, View view, int type) {
    scroller.fullScroll(FOCUS_RIGHT);
  }
}
