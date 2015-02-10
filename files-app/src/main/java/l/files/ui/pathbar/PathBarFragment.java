package l.files.ui.pathbar;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l.files.R;
import l.files.fs.Path;
import l.files.operations.Events;
import l.files.ui.OpenFileRequest;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static l.files.ui.IconFonts.getDirectoryIcon;

public final class PathBarFragment extends Fragment
    implements LoaderCallbacks<PathBarFragment.Hierarchy>, OnClickListener {

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

  @Override public Loader<Hierarchy> onCreateLoader(int id, Bundle args) {
    final Path path = this.path;
    return new AsyncTaskLoader<Hierarchy>(getActivity()) {
      @Override public Hierarchy loadInBackground() {
        List<Path> hierarchy = new ArrayList<>();
        for (Path p = path; p != null; p = p.getParent()) {
          hierarchy.add(p);
        }
        Collections.reverse(hierarchy);
        return new Hierarchy(hierarchy);
      }

      @Override protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
      }
    };
  }

  @Override public void onLoadFinished(Loader<Hierarchy> loader,
                                       Hierarchy hierarchy) {
    updatePathBar(hierarchy);
  }

  private void updatePathBar(Hierarchy hierarchy) {
    LayoutInflater inflater = LayoutInflater.from(getActivity());

    int i = 0;
    for (Path path : hierarchy.paths) {
      View view = container.getChildAt(i);
      if (view == null) {
        view = inflater.inflate(R.layout.path_bar_item, container, false);
        container.addView(view);
      }

      view.setTag(path);
      view.setVisibility(VISIBLE);
      view.setOnClickListener(this);

      TextView title = (TextView) view.findViewById(R.id.title);
      title.setText(path.getParent() == null ? Build.MODEL : path.getPath().getName());

      AssetManager asset = getActivity().getAssets();
      TextView icon = (TextView) view.findViewById(R.id.icon);
      icon.setTypeface(getDirectoryIcon(asset, path.getPath()));

    }

    for (; i < container.getChildCount(); i++) {
      container.getChildAt(i).setVisibility(GONE);
    }
  }

  @Override public void onLoaderReset(Loader<Hierarchy> loader) {}

  @Override public void onClick(View v) {
    Path status = (Path) v.getTag();
    Events.get().post(new OpenFileRequest(status.getPath()));
  }

  static final class Hierarchy {
    final List<Path> paths;

    private Hierarchy(List<Path> paths) {
      this.paths = paths;
    }
  }

}
