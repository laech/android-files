package l.files.ui.browser;

import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import l.files.base.Throwables;
import l.files.fs.Path;
import l.files.fs.TraversalCallback;
import l.files.testing.fs.Paths;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static android.content.Intent.ACTION_MAIN;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Environment.getExternalStorageDirectory;
import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static java.lang.System.currentTimeMillis;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.TraversalCallback.Result.CONTINUE;
import static l.files.ui.browser.FilesActivity.EXTRA_DIRECTORY;
import static l.files.ui.browser.FilesActivity.EXTRA_WATCH_LIMIT;
import static org.junit.Assert.*;

public class BaseFilesActivityTest {

    @Rule
    public final ActivityTestRule<FilesActivity> activityTestRule =
        new ActivityTestRule<FilesActivity>(FilesActivity.class, false, false) {
            @Nullable
            @Override
            protected Intent getActivityIntent() {
                return activityIntent;
            }
        };


    @Nullable
    private Path dir;

    @Nullable
    private UiFileActivity screen;

    @Nullable
    private Intent activityIntent;

    private File createTempFolder() throws IOException {
        // On Nexus S it was observed that File.createTempFile(prefix, null)
        // could return the name directory as the previous run of the test,
        // so add a suffix to make it more randomized
        String prefix = getClass().getSimpleName();
        String suffix = String.valueOf(currentTimeMillis());
        File dir = File.createTempFile(prefix, suffix);
        assertTrue(dir.delete());
        assertTrue(dir.mkdir());
        return dir;
    }

    @Before
    public void setUp() throws Exception {
        dir = Path.of(createTempFolder());
        setActivityIntent(newIntent(dir));
        screen = new UiFileActivity(
            getInstrumentation(),
            () -> getActivity()
        );
    }

    @After
    public void tearDown() throws Exception {
        screen = null;
        if (dir != null && dir.exists(NOFOLLOW_LINKS)) {
            dir.traverse(NOFOLLOW, delete());
        }
        dir = null;
    }

    FilesActivity getActivity() {
        FilesActivity activity = activityTestRule.getActivity();
        if (activity == null) {
            activity = activityTestRule.launchActivity(activityIntent);
        }
        allowPermissionsIfNeeded();
        return activity;
    }

    void setActivityIntent(Intent intent) {
        this.activityIntent = intent;
    }

    void runTestOnUiThread(Runnable runnable) throws Throwable {
        activityTestRule.runOnUiThread(runnable);
    }

    private static boolean permissionAllowed;

    private void allowPermissionsIfNeeded() {

        // Only do this once otherwise will slow down every test
        if (SDK_INT >= M && !permissionAllowed) {
            permissionAllowed = true;
            UiDevice device =
                UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            UiObject allowPermissions =
                device.findObject(new UiSelector().textMatches("^(?i)allow$"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    throw new AssertionError(e);
                }
            }
        }
    }

    private TraversalCallback<Path> delete() {
        return new TraversalCallback.Base<Path>() {

            @Override
            public Result onPreVisit(Path file) {
                try {
                    file.setPermissions(EnumSet.allOf(PosixFilePermission.class));
                } catch (IOException ignored) {
                }
                return CONTINUE;
            }

            @Override
            public Result onPostVisit(Path file) throws IOException {
                file.delete();
                return CONTINUE;
            }

        };
    }

    UiFileActivity screen() {
        getActivity();
        assert screen != null;
        return screen;
    }

    Path dir() {
        assert dir != null;
        return dir;
    }

    private Intent newIntent(Path dir) {
        return newIntent(dir, -1);
    }

    Intent newIntent(Path dir, int watchLimit) {
        return new Intent(ACTION_MAIN)
            .putExtra(EXTRA_DIRECTORY, dir)
            .putExtra(EXTRA_WATCH_LIMIT, watchLimit);
    }

    static final class CannotRenameFileToDifferentCasing
        extends RuntimeException {
        CannotRenameFileToDifferentCasing(String detailMessage) {
            super(detailMessage);
        }
    }

    Path createCaseInsensitiveFileSystemDir(String name) throws IOException {

        /*
         * Use getExternalStorageDirectory() for this test, as this issue
         * is only reproducible there (this test assumes this, making it
         * less reliable).
         *
         * The bug: "a" gets renamed to "A", instead of displaying only
         * "A", both "a" and "A" are displayed.
         */
        Path dir = Path.of(getExternalStorageDirectory()).concat(name);
        Path src = dir.concat("z");
        Path dst = dir.concat("Z");
        try {

            Paths.deleteRecursiveIfExists(dir);
            Paths.createFiles(src);

            assertTrue(src.exists(NOFOLLOW_LINKS));
            assertTrue(
                "Assuming the underlying file system is case insensitive",
                dst.exists(NOFOLLOW_LINKS)
            );

            src.move(dst);
            List<Path> actual = dir.list(new ArrayList<Path>());
            List<Path> expected = Collections.singletonList(dst);
            assertEquals(1, actual.size());
            if (!expected.equals(actual)) {
                throw new CannotRenameFileToDifferentCasing(
                    "\nexpected: " + expected + "\nactual: " + actual);
            }

            setActivityIntent(newIntent(dir));

        } catch (Throwable e) {
            try {
                Paths.deleteRecursiveIfExists(dir);
            } catch (Throwable sup) {
                Throwables.addSuppressed(e, sup);
            }
            throw e;

        } finally {

            try {
                Paths.deleteIfExists(src);
            } catch (IOException ignore) {
            }

            try {
                Paths.deleteIfExists(dst);
            } catch (IOException ignore) {
            }

        }

        assertFalse(src.exists(NOFOLLOW_LINKS));
        assertFalse(dst.exists(NOFOLLOW_LINKS));

        return dir;
    }

}
