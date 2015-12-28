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

import l.files.base.Provider;
import l.files.base.Throwables;
import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Paths;
import l.files.fs.Permission;
import l.files.fs.TraversalCallback;

import static android.content.Intent.ACTION_MAIN;
import static android.os.Environment.getExternalStorageDirectory;
import static java.util.Collections.singletonList;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.TraversalCallback.Result.CONTINUE;
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

    private Path dir;
    private Intent intent;
    private UiFileActivity screen;

    @Before
    public void setUp() throws Exception {
        String name = testName.getMethodName();
        if (name.length() > 255) {
            name = name.substring(0, 255);
        }
        dir = Paths.get(folder.newFolder(name));
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
        if (Files.exists(dir, NOFOLLOW)) {
            Files.traverse(dir, NOFOLLOW, delete());
        }
        dir = null;
    }

    private TraversalCallback<Path> delete() {
        return new TraversalCallback.Base<Path>() {

            @Override
            public Result onPreVisit(Path file) throws IOException {
                try {
                    Files.setPermissions(file, Permission.all());
                } catch (IOException ignored) {
                }
                return CONTINUE;
            }

            @Override
            public Result onPostVisit(Path file) throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

        };
    }

    UiFileActivity screen() {
        getActivity();
        return screen;
    }

    Path dir() {
        return dir;
    }

    Intent newIntent(Path dir) {
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

    Path createCaseInsensitiveFileSystemDir() throws IOException {

        /*
         * Use getExternalStorageDirectory() for this test, as this issue
         * is only reproducible there (this test assumes this, making it
         * less reliable).
         *
         * The bug: "a" gets renamed to "A", instead of displaying only
         * "A", both "a" and "A" are displayed.
         */
        Path dir = Paths.get(getExternalStorageDirectory())
                .resolve(testName.getMethodName());

        Path src = dir.resolve("z");
        Path dst = dir.resolve("Z");
        try {

            Files.deleteRecursiveIfExists(dir);
            Files.createFiles(src);

            assertTrue(Files.exists(src, NOFOLLOW));
            assumeTrue(
                    "Assuming the underlying file system is case insensitive",
                    Files.exists(dst, NOFOLLOW));

            Files.move(src, dst);
            List<Path> actual = Files.list(dir, NOFOLLOW, new ArrayList<Path>());
            assertEquals(1, actual.size());
            assumeThat(
                    "Assuming the file can be renamed to different casing",
                    actual,
                    equalTo(singletonList(dst)));

            setActivityIntent(newIntent(dir));

        } catch (Throwable e) {
            try {
                Files.deleteRecursiveIfExists(dir);
            } catch (Throwable sup) {
                Throwables.addSuppressed(e, sup);
            }
            throw e;

        } finally {

            try {
                Files.deleteIfExists(src);
            } catch (IOException ignore) {
            }

            try {
                Files.deleteIfExists(dst);
            } catch (IOException ignore) {
            }

        }

        assertFalse(Files.exists(src, NOFOLLOW));
        assertFalse(Files.exists(dst, NOFOLLOW));

        return dir;
    }

}
