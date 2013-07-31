package l.files.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ActionMode;
import android.view.View;
import l.files.app.FilesFragment;

public final class TestFilesFragmentActivity extends FragmentActivity {

  public static final String DIRECTORY = "directory";

  private FilesFragment fragment;
  private ActionMode mode;

  public FilesFragment getFragment() {
    return fragment;
  }

  public ActionMode getActionMode() {
    return mode;
  }

  @Override
  public void onActionModeStarted(ActionMode mode) {
    super.onActionModeStarted(mode);
    this.mode = mode;
  }

  @Override
  public void onActionModeFinished(ActionMode mode) {
    super.onActionModeFinished(mode);
    this.mode = null;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View view = new View(this);
    view.setId(android.R.id.content);
    setContentView(view);

    fragment = new FilesFragment();
    fragment.setArguments(getIntent().getExtras());
    getSupportFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, fragment)
        .commit();
  }
}
