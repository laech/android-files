package l.files.ui.rename;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.widget.EditText;

import java.io.IOException;

import l.files.R;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.operations.Events;
import l.files.ui.CloseActionModeRequest;
import l.files.ui.FileCreationFragment;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.IOExceptions.message;

public final class RenameFragment extends FileCreationFragment {

  public static final String TAG = RenameFragment.class.getSimpleName();

  private static final String ARG_RESOURCE = "resource";

  static RenameFragment create(Resource resource) {
    Bundle args = new Bundle(2);
    args.putParcelable(ARG_PARENT_RESOURCE, resource.parent());
    args.putParcelable(ARG_RESOURCE, resource);

    RenameFragment fragment = new RenameFragment();
    fragment.setArguments(args);
    return fragment;
  }

  private AsyncTask<?, ?, ?> highlight;
  private AsyncTask<?, ?, ?> rename;

  @Override public void onDestroy() {
    super.onDestroy();

    if (highlight != null) {
      highlight.cancel(true);
    }
    if (rename != null) {
      rename.cancel(true);
    }
  }

  @Override public void onStart() {
    super.onStart();
    highlight();
  }

  private Resource resource() {
    return getArguments().getParcelable(ARG_RESOURCE);
  }

  private void highlight() {
    if (getFilename().isEmpty()) {
      highlight = new Highlight()
          .executeOnExecutor(THREAD_POOL_EXECUTOR, resource());
    }
  }

  private class Highlight extends AsyncTask<Resource, Void, Pair<Resource, Stat>> {

    @Override
    protected Pair<Resource, Stat> doInBackground(Resource... params) {
      Resource resource = params[0];
      try {
        return Pair.create(resource, resource.stat(NOFOLLOW));
      } catch (IOException e) {
        return null;
      }
    }

    @Override protected void onPostExecute(Pair<Resource, Stat> pair) {
      super.onPostExecute(pair);
      if (pair != null) {
        Resource resource = pair.first;
        Stat stat = pair.second;
        EditText field = getFilenameField();
        if (!getFilename().isEmpty()) {
          return;
        }
        field.setText(resource.name());
        if (stat.isDirectory()) {
          field.selectAll();
        } else {
          field.setSelection(0, resource.name().base().length());
        }
      }
    }
  }

  @Override protected CharSequence getError(Resource target) {
    if (resource().equals(target)) {
      return null;
    }
    return super.getError(target);
  }

  @Override protected int getTitleResourceId() {
    return R.string.rename;
  }

  @Override public void onClick(DialogInterface dialog, int which) {
    rename();
  }

  private void rename() {
    Resource dst = parent().resolve(getFilename());
    rename = new Rename()
        .executeOnExecutor(THREAD_POOL_EXECUTOR, resource(), dst);
    Events.get().post(CloseActionModeRequest.INSTANCE);
  }

  private class Rename extends AsyncTask<Resource, Void, IOException> {

    @Override protected IOException doInBackground(Resource... params) {
      Resource src = params[0];
      Resource dst = params[1];
      try {
        src.moveTo(dst);
        return null;
      } catch (IOException e) {
        return e;
      }
    }

    @Override protected void onPostExecute(IOException e) {
      super.onPostExecute(e);
      if (e != null) {
        toaster.apply(message(e));
      }
    }
  }

}
