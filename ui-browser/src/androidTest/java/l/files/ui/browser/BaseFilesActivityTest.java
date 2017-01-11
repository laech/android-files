package l.files.ui.browser;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import l.files.base.Provider;
import l.files.base.Throwables;
import l.files.fs.FileSystem;
import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.TraversalCallback;
import l.files.fs.local.LocalFileSystem;
import l.files.testing.fs.Files;

import static android.content.Intent.ACTION_MAIN;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Environment.getExternalStorageDirectory;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static java.util.Collections.singletonList;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.TraversalCallback.Result.CONTINUE;
import static l.files.ui.browser.FilesActivity.EXTRA_DIRECTORY;
import static l.files.ui.browser.FilesActivity.EXTRA_WATCH_LIMIT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    final FileSystem fs = LocalFileSystem.INSTANCE;

    @Nullable
    private Path dir;

    @Nullable
    private UiFileActivity screen;

    @Nullable
    private Intent activityIntent;

    private File createTempFolder() throws IOException {
        File dir = File.createTempFile(getClass().getSimpleName(), null);
        assertTrue(dir.delete());
        assertTrue(dir.mkdir());
        return dir;
    }

    @Before
    public void setUp() throws Exception {
        dir = Path.fromFile(createTempFolder());
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

    @After
    public void tearDown() throws Exception {
        screen = null;
        if (dir != null && fs.exists(dir, NOFOLLOW)) {
            fs.traverse(dir, NOFOLLOW, delete());
        }
        dir = null;
    }

    public FilesActivity getActivity() {
        FilesActivity activity = activityTestRule.getActivity();
        if (activity == null) {
            activity = activityTestRule.launchActivity(activityIntent);
        }
        allowPermissionsIfNeeded();
        return activity;
    }

    public void setActivityIntent(Intent intent) {
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
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            UiObject allowPermissions = device.findObject(new UiSelector().resourceId(
                    "com.android.packageinstaller:id/permission_allow_button"));
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
            public Result onPreVisit(Path file) throws IOException {
                try {
                    fs.setPermissions(file, Permission.all());
                } catch (IOException ignored) {
                }
                return CONTINUE;
            }

            @Override
            public Result onPostVisit(Path file) throws IOException {
                fs.delete(file);
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

    Intent newIntent(Path dir) {
        return newIntent(dir, -1);
    }

    Intent newIntent(Path dir, int watchLimit) {
        return new Intent(ACTION_MAIN)
                .putExtra(EXTRA_DIRECTORY, dir)
                .putExtra(EXTRA_WATCH_LIMIT, watchLimit);
    }

    static final class CannotRenameFileToDifferentCasing extends RuntimeException {
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
        Path dir = Path.fromFile(getExternalStorageDirectory()).concat(name);
        Path src = dir.concat("z");
        Path dst = dir.concat("Z");
        try {

            Files.deleteRecursiveIfExists(fs, dir);
            Files.createFiles(fs, src);

            assertTrue(fs.exists(src, NOFOLLOW));
            assertTrue(
                    "Assuming the underlying file system is case insensitive",
                    fs.exists(dst, NOFOLLOW));

            fs.move(src, dst);
            List<Path> actual = fs.list(dir, NOFOLLOW, new ArrayList<Path>());
            List<Path> expected = singletonList(dst);
            assertEquals(1, actual.size());
            if (!expected.equals(actual)) {
                throw new CannotRenameFileToDifferentCasing(
                        "\nexpected: " + expected + "\nactual: " + actual);
            }

            setActivityIntent(newIntent(dir));

        } catch (Throwable e) {
            try {
                Files.deleteRecursiveIfExists(fs, dir);
            } catch (Throwable sup) {
                Throwables.addSuppressed(e, sup);
            }
            throw e;

        } finally {

            try {
                Files.deleteIfExists(fs, src);
            } catch (IOException ignore) {
            }

            try {
                Files.deleteIfExists(fs, dst);
            } catch (IOException ignore) {
            }

        }

        assertFalse(fs.exists(src, NOFOLLOW));
        assertFalse(fs.exists(dst, NOFOLLOW));

        return dir;
    }

}
