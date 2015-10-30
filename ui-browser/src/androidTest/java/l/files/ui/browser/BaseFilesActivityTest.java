package l.files.ui.browser;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.UiThreadTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Permission;
import l.files.fs.Visitor;
import l.files.fs.local.LocalFile;

import static android.content.Intent.ACTION_MAIN;
import static java.io.File.createTempFile;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.ui.browser.FilesActivity.EXTRA_DIRECTORY;
import static org.junit.Assert.assertTrue;

public class BaseFilesActivityTest {

    @Rule
    public final ActivityTestRule<FilesActivity> activity =
            new ActivityTestRule<>(FilesActivity.class, false, false);

    @Rule
    public final UiThreadTestRule uiThread = new UiThreadTestRule();

    @Rule
    public final TestName testName = new TestName();

    private File dir;
    private Intent intent;
    private UiFileActivity screen;

    @Before
    public void setUp() throws Exception {
        String name = testName.getMethodName();
        if (name.length() > 100) {
            name = name.substring(0, 100);
        }
        dir = LocalFile.of(createTempDir(name));
        setActivityIntent(newIntent(dir));
        screen = new UiFileActivity(
                getInstrumentation(),
                new Provider<FilesActivity>() {
                    @Override
                    public FilesActivity get() {
                        return getActivity();
                    }
                });
    }

    private void setActivityIntent(Intent intent) {
        this.intent = intent;
    }

    private java.io.File createTempDir(String name) throws IOException {
        java.io.File file = createTempFile(name, "");
        assertTrue(file.delete());
        assertTrue(file.mkdir());
        return file;
    }

    @After
    public void tearDown() throws Exception {
        screen = null;
        if (dir.exists(NOFOLLOW)) {
            dir.traverse(NOFOLLOW, delete());
        }
        dir = null;
    }

    private Visitor delete() {
        return new Visitor.Base() {

            @Override
            public Result onPreVisit(File file) throws IOException {
                try {
                    file.setPermissions(Permission.all());
                } catch (IOException ignored) {
                }
                return CONTINUE;
            }

            @Override
            public Result onPostVisit(File file) throws IOException {
                file.delete();
                return CONTINUE;
            }

        };
    }

    UiFileActivity screen() {
        getActivity();
        return screen;
    }

    File dir() {
        return dir;
    }

    private Intent newIntent(File dir) {
        return new Intent(ACTION_MAIN).putExtra(EXTRA_DIRECTORY, dir);
    }

    Instrumentation getInstrumentation() {
        return InstrumentationRegistry.getInstrumentation();
    }

    FilesActivity getActivity() {
        if (activity.getActivity() == null) {
            activity.launchActivity(intent);
        }
        return activity.getActivity();
    }

    void runTestOnUiThread(Runnable runnable) throws Throwable {
        uiThread.runOnUiThread(runnable);
    }

}
