package l.files.app;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import l.files.R;

import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static android.view.View.FOCUS_RIGHT;
import static l.files.provider.FilesContract.FileInfo.COLUMN_NAME;
import static l.files.provider.FilesContract.buildHierarchyUri;

public final class PathBarFragment
    extends Fragment implements LoaderCallbacks<Cursor>, View.OnClickListener {

  private String fileId;

  private HorizontalScrollView scrollView;
  private ViewGroup itemContainer;

  Bus bus;

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
    scrollView = (HorizontalScrollView) root.findViewById(R.id.path_bar_scroll);
    itemContainer = (ViewGroup) root.findViewById(R.id.path_item_container);
    bus = FilesApp.getBus(this);
    if (fileId != null) {
      getLoaderManager().restartLoader(0, null, this);
    }
  }

  @Override public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    Uri uri = buildHierarchyUri(fileId);
    return new CursorLoader(getActivity(), uri, null, null, null, null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    itemContainer.removeAllViewsInLayout();
    if (cursor.moveToFirst()) {
      LayoutInflater inflater = LayoutInflater.from(getActivity());
      do {
        View view = inflater.inflate(R.layout.path_bar_item, itemContainer, false);
        itemContainer.addView(view);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
        view.setTag(OpenFileRequest.from(cursor));
        view.setOnClickListener(this);
      } while (cursor.moveToNext());
      scrollView.post(new Runnable() {
        @Override public void run() {
          scrollView.fullScroll(FOCUS_RIGHT);
        }
      });
    }
  }

  @Override public void onLoaderReset(Loader<Cursor> loader) {}

  @Override public void onClick(View v) {
    bus.post(v.getTag());
  }
}
