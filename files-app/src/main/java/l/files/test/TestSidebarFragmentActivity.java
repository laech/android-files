package l.files.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import l.files.app.SidebarFragment;

public final class TestSidebarFragmentActivity extends Activity {

  private SidebarFragment fragment;

  public SidebarFragment getFragment() {
    return fragment;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View view = new View(this);
    view.setId(android.R.id.content);
    setContentView(view);

    fragment = new SidebarFragment();
    getFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, fragment)
        .commit();
  }
}
