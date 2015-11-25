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
import java.util.ArrayList;
import java.util.List;

import l.files.base.Throwables;
import l.files.fs.File;
import l.files.fs.Permission;
import l.files.fs.Visitor;
import l.files.fs.local.LocalFile;

import static android.content.Intent.ACTION_MAIN;
import static android.os.Environment.getExternalStorageDirectory;
import static java.util.Collections.singletonList;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.ui.browser.FilesActivity.EXTRA_DIRECTORY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

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

    Intent newIntent(File dir) {
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

    File createCaseInsensitiveFileSystemDir() throws IOException {

        /*
         * Use getExternalStorageDirectory() for this test, as this issue
         * is only reproducible there (this test assumes this, making it
         * less reliable).
         *
         * The bug: "a" gets renamed to "A", instead of displaying only
         * "A", both "a" and "A" are displayed.
         */
        File dir = LocalFile.of(getExternalStorageDirectory())
                .resolve(testName.getMethodName());

        File src = dir.resolve("z");
        File dst = dir.resolve("Z");
        try {

            dir.deleteRecursiveIfExists();
            src.createFiles();

            assertTrue(src.exists(NOFOLLOW));
            assumeTrue(
                    "Assuming the underlying file system is case insensitive",
                    dst.exists(NOFOLLOW));

            src.moveTo(dst);
            List<File> actual = dir.list(NOFOLLOW).to(new ArrayList<File>());
            assertEquals(1, actual.size());
            assumeThat(
                    "Assuming the file can be renamed to different casing",
                    actual,
                    equalTo(singletonList(dst)));

            setActivityIntent(newIntent(dir));

        } catch (Throwable e) {
            try {
                dir.deleteRecursiveIfExists();
            } catch (Throwable sup) {
                Throwables.addSuppressed(e, sup);
            }
            throw e;

        } finally {

            try {
                src.deleteIfExists();
            } catch (IOException ignore) {
            }

            try {
                dst.deleteIfExists();
            } catch (IOException ignore) {
            }

        }

        assertFalse(src.exists(NOFOLLOW));
        assertFalse(dst.exists(NOFOLLOW));

        return dir;
    }

}
