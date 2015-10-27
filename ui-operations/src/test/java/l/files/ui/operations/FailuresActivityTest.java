package l.files.ui.operations;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import java.util.ArrayList;

import l.files.fs.File;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static java.util.Arrays.asList;
import static l.files.ui.operations.FailuresActivity.EXTRA_FAILURES;
import static l.files.ui.operations.FailuresActivity.EXTRA_TITLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = JELLY_BEAN)
public final class FailuresActivityTest {

    @Test
    public void sets_title_from_intent() {
        String title = "hello";
        ActionBar actionBar = buildActivity()
                .withIntent(newIntent().putExtra(EXTRA_TITLE, title))
                .create()
                .get()
                .getSupportActionBar();

        assertNotNull(actionBar);
        assertEquals(title, actionBar.getTitle());
    }

    @Test
    public void sets_failures_from_intent() {
        FailureMessage f1 = FailureMessage.create(mockFile("/a"), "test1");
        FailureMessage f2 = FailureMessage.create(mockFile("/b"), "test2");
        ListView list = (ListView) buildActivity()
                .withIntent(newIntent().putParcelableArrayListExtra(EXTRA_FAILURES, new ArrayList<>(asList(f1, f2))))
                .create()
                .postCreate(null)
                .visible()
                .get()
                .findViewById(android.R.id.list);

        assertEquals(f1, list.getItemAtPosition(0));
        assertEquals(f2, list.getItemAtPosition(1));
        assertFailureView(f1, list.getChildAt(0));
        assertFailureView(f2, list.getChildAt(1));
    }

    private File mockFile(String path) {
        File file = mock(File.class);
        given(file.path()).willReturn(path);
        return file;
    }


    private void assertFailureView(FailureMessage msg, View view) {
        assertEquals(msg.file().path(), ((TextView) view.findViewById(R.id.failure_path)).getText());
        assertEquals(msg.message(), ((TextView) view.findViewById(R.id.failure_message)).getText());
    }

    @Test
    public void home_as_up_is_displayed() {
        ActionBar actionBar = buildActivity().withIntent(newIntent()).create().get().getSupportActionBar();
        assertNotNull(actionBar);
        assertTrue((actionBar.getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) != 0);
    }

    private ActivityController<FailuresActivity> buildActivity() {
        return Robolectric.buildActivity(FailuresActivity.class);
    }

    private Intent newIntent() {
        return new Intent()
                .putExtra(EXTRA_TITLE, "abc")
                .putParcelableArrayListExtra(EXTRA_FAILURES, new ArrayList<Parcelable>());
    }

}
