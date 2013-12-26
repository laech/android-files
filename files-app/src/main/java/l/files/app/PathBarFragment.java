package l.files.app;

import android.content.res.AssetManager;
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
import android.widget.TextView;

import l.files.R;
import l.files.analytics.Analytics;

import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static l.files.provider.FileCursors.getLocation;
import static l.files.provider.FileCursors.getName;
import static l.files.provider.FileCursors.isDirectory;
import static l.files.provider.FileCursors.isReadable;
import static l.files.provider.FilesContract.buildHierarchyUri;

public final class PathBarFragment extends Fragment
    implements LoaderCallbacks<Cursor>, OnClickListener {

  private String fileLocation;
  private ViewGroup container;

  public void set(String fileLocation) {
    this.fileLocation = fileLocation;
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
    container = (ViewGroup) root.findViewById(R.id.path_item_container);
    if (fileLocation != null) {
      getLoaderManager().restartLoader(0, null, this);
    }
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Uri uri = buildHierarchyUri(fileLocation);
    return new CursorLoader(getActivity(), uri, null, null, null, null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    updatePathBar(cursor);
  }

  private void updatePathBar(Cursor cursor) {
    if (cursor.moveToFirst()) {
      LayoutInflater inflater = LayoutInflater.from(getActivity());

      do {
        View view = container.getChildAt(cursor.getPosition());
        if (view == null) {
          view = inflater.inflate(R.layout.path_bar_item, container, false);
          container.addView(view);
        }
        view.setTag(R.id.file_id, getLocation(cursor));
        view.setTag(R.id.file_name, getName(cursor));
        view.setTag(R.id.is_readable, isReadable(cursor));
        view.setTag(R.id.is_directory, isDirectory(cursor));
        view.setVisibility(VISIBLE);
        view.setOnClickListener(this);

        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(cursor.isFirst() ? Build.MODEL : getName(cursor));

        AssetManager asset = getActivity().getAssets();
        TextView icon = (TextView) view.findViewById(R.id.icon);
        icon.setTypeface(IconFonts.forDirectoryLocation(asset, getLocation(cursor)));

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

    Analytics.onEvent(getActivity(), "path_bar", "click");
  }
}
