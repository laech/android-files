package l.files.operations.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Parcelable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import l.files.R;
import l.files.common.testing.BaseActivityTest;
import l.files.fs.local.LocalResource;

import static java.util.Arrays.asList;
import static l.files.operations.ui.FailuresActivity.EXTRA_FAILURES;
import static l.files.operations.ui.FailuresActivity.EXTRA_TITLE;

public final class FailuresActivityTest extends BaseActivityTest<FailuresActivity> {

  public FailuresActivityTest() {
    super(FailuresActivity.class);
  }

  public void testSetsTitleFromIntent() {
    String title = "hello";
    setActivityIntent(newIntent().putExtra(EXTRA_TITLE, title));
    assertEquals(title, getActivity().getTitle());
  }

  public void testSetsFailuresFromIntent() {
    FailureMessage f1 = FailureMessage.create(LocalResource.create(new File("1")), "test1");
    FailureMessage f2 = FailureMessage.create(LocalResource.create(new File("2")), "test2");
    setActivityIntent(newIntent().putParcelableArrayListExtra(EXTRA_FAILURES, new ArrayList<>(asList(f1, f2))));

    ListView list = (ListView) getActivity().findViewById(android.R.id.list);
    assertEquals(f1, list.getItemAtPosition(0));
    assertEquals(f2, list.getItemAtPosition(1));
    assertFailureView(f1, list.getChildAt(0));
    assertFailureView(f2, list.getChildAt(1));
  }

  private void assertFailureView(FailureMessage msg, View view) {
    assertEquals(msg.resource().toString(), ((TextView) view.findViewById(R.id.failure_path)).getText());
    assertEquals(msg.message(), ((TextView) view.findViewById(R.id.failure_message)).getText());
  }

  public void testHomeAsUpIsDisplayed() {
    setActivityIntent(newIntent());
    ActionBar actionBar = getActivity().getActionBar();
    assertNotNull(actionBar);
    assertTrue((actionBar.getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) != 0);
  }

  public Intent newIntent() {
    return new Intent()
        .putExtra(EXTRA_TITLE, "abc")
        .putParcelableArrayListExtra(EXTRA_FAILURES, new ArrayList<Parcelable>());
  }

}
