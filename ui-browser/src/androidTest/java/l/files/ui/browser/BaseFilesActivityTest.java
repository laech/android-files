package l.files.ui.browser;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.UiThreadTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Permission;
import l.files.fs.Visitor;
import l.files.fs.local.LocalFile;

import static android.content.Intent.ACTION_MAIN;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.ui.browser.FilesActivity.EXTRA_DIRECTORY;

public class BaseFilesActivityTest {

    @Rule
    public final ActivityTestRule<FilesActivity> activity =
            new ActivityTestRule<>(FilesActivity.class, false, false);

    @Rule
    public final UiThreadTestRule uiThread = new UiThreadTestRule();

    @Rule
    public final TestName testName = new TestName();

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final TakeScreenShotOnFailure screenshot =
            new TakeScreenShotOnFailure();

    private LocalFile dir;
    private Intent intent;
    private UiFileActivity screen;

    @Before
    public void setUp() throws Exception {
        String name = testName.getMethodName();
        if (name.length() > 255) {
            name = name.substring(0, 255);
        }
        dir = LocalFile.of(folder.newFolder(name));
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

    protected void setActivityIntent(Intent intent) {
        this.intent = intent;
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

    LocalFile dir() {
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
