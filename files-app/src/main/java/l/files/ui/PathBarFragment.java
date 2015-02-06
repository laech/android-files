package l.files.ui;

import android.app.Fragment;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l.files.R;
import l.files.fs.FileSystemException;
import l.files.fs.Path;
import l.files.fs.ResourceStatus;
import l.files.logging.Logger;
import l.files.operations.Events;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static java.util.Collections.emptyList;
import static l.files.ui.IconFonts.getDirectoryIcon;

public final class PathBarFragment extends Fragment
    implements LoaderCallbacks<List<ResourceStatus>>, OnClickListener {

  private static final Logger log = Logger.get(PathBarFragment.class);

  private Path path;
  private ViewGroup container;

  public void set(Path path) {
    this.path = path;
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
    if (path != null) {
      getLoaderManager().restartLoader(0, null, this);
    }
  }

  @Override public Loader<List<ResourceStatus>> onCreateLoader(int id, Bundle args) {
    final Path path = this.path;
    return new AsyncTaskLoader<List<ResourceStatus>>(getActivity()) {
      @Override public List<ResourceStatus> loadInBackground() {
        List<ResourceStatus> hierarchy = new ArrayList<>();
        for (Path p = path; p != null; p = p.getParent()) {
          try {
            hierarchy.add(p.getResource().readStatus(true));
          } catch (IOException | FileSystemException e) { // TODO
            log.error(e);
            return emptyList();
          }
        }
        Collections.reverse(hierarchy);
        return hierarchy;
      }

      @Override protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
      }
    };
  }

  @Override public void onLoadFinished(Loader<List<ResourceStatus>> loader,
                                       List<ResourceStatus> hierarchy) {
    updatePathBar(hierarchy);
  }

  private void updatePathBar(List<ResourceStatus> hierarchy) {
    LayoutInflater inflater = LayoutInflater.from(getActivity());

    int i = 0;
    for (; i < hierarchy.size(); i++) {
      View view = container.getChildAt(i);
      if (view == null) {
        view = inflater.inflate(R.layout.path_bar_item, container, false);
        container.addView(view);
      }

      ResourceStatus stat = hierarchy.get(i);
      view.setTag(stat);
      view.setVisibility(VISIBLE);
      view.setOnClickListener(this);

      TextView title = (TextView) view.findViewById(R.id.title);
      title.setText(i == 0 ? Build.MODEL : stat.getPath().getName());

      AssetManager asset = getActivity().getAssets();
      TextView icon = (TextView) view.findViewById(R.id.icon);
      icon.setTypeface(getDirectoryIcon(asset, stat.getPath()));

    }

    for (; i < container.getChildCount(); i++) {
      container.getChildAt(i).setVisibility(GONE);
    }
  }

  @Override public void onLoaderReset(Loader<List<ResourceStatus>> loader) {}

  @Override public void onClick(View v) {
    ResourceStatus status = (ResourceStatus) v.getTag();
    Events.get().post(new OpenFileRequest(status.getPath()));
  }

}
