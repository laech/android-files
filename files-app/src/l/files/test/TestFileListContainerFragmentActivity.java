package l.files.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import l.files.app.FilesPagerFragment;

public final class TestFileListContainerFragmentActivity extends FragmentActivity {

  public static final String DIRECTORY = FilesPagerFragment.ARG_DIRECTORY;

  private FilesPagerFragment fragment;

  public FilesPagerFragment fragment() {
    return fragment;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View view = new View(this);
    view.setId(android.R.id.content);
    setContentView(view);

    fragment = new FilesPagerFragment();
    fragment.setArguments(getIntent().getExtras());
    getSupportFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, fragment)
        .commit();
  }
}
