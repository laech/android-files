package l.files.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import l.files.R;
import l.files.operations.Events;
import l.files.ui.analytics.Analytics;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static l.files.provider.FilesContract.Files;
import static l.files.provider.FilesContract.getHierarchyUri;

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
    assert root != null;
    container = (ViewGroup) root.findViewById(R.id.path_item_container);
    if (fileLocation != null) {
      getLoaderManager().restartLoader(0, null, this);
    }
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Activity context = getActivity();
    Uri uri = getHierarchyUri(context, fileLocation);
    return new CursorLoader(context, uri, null, null, null, null);
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
        view.setTag(R.id.file_id, Files.id(cursor));
        view.setTag(R.id.file_name, Files.name(cursor));
        view.setTag(R.id.is_readable, Files.isReadable(cursor));
        view.setTag(R.id.is_directory, Files.isDirectory(cursor));
        view.setVisibility(VISIBLE);
        view.setOnClickListener(this);

        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(cursor.isFirst() ? Build.MODEL : Files.name(cursor));

        AssetManager asset = getActivity().getAssets();
        TextView icon = (TextView) view.findViewById(R.id.icon);
        icon.setTypeface(IconFonts.forDirectoryLocation(asset, Files.id(cursor)));

      } while (cursor.moveToNext());
      for (int i = cursor.getPosition(); i < container.getChildCount(); i++) {
        container.getChildAt(i).setVisibility(GONE);
      }
    }
  }

  @Override public void onLoaderReset(Loader<Cursor> loader) {}

  @Override public void onClick(View v) {
    Events.get().post(OpenFileRequest.create(
        (String) v.getTag(R.id.file_id),
        (String) v.getTag(R.id.file_name),
        (boolean) v.getTag(R.id.is_readable),
        (boolean) v.getTag(R.id.is_directory)));

    Analytics.onEvent(getActivity(), "path_bar", "click");
  }
}