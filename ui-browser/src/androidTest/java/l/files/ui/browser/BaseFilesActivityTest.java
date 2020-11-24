package l.files.ui.browser;

import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import static android.content.Intent.ACTION_MAIN;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Environment.getExternalStorageDirectory;
import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static java.lang.System.currentTimeMillis;
import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.stream.Collectors.toList;
import static l.files.testing.fs.Paths.createFiles;
import static l.files.testing.fs.Paths.deleteRecursiveIfExists;
import static l.files.ui.browser.FilesActivity.EXTRA_DIRECTORY;
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
        dir = createTempFolder().toPath();
        setActivityIntent(newIntent(dir));
        screen = new UiFileActivity(
            getInstrumentation(),
            this::getActivity
        );
    }

    @After
    public void tearDown() throws Exception {
        screen = null;
        if (dir != null && exists(dir, NOFOLLOW_LINKS)) {
            setPosixFilePermissions(
                dir,
                EnumSet.allOf(PosixFilePermission.class)
            );
            walkFileTree(dir, deleter());
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

    private FileVisitor<Path> deleter() {
        return new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(
                Path file,
                BasicFileAttributes attrs
            ) throws IOException {
                setPermissions(file);
                delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult preVisitDirectory(
                Path dir,
                BasicFileAttributes attrs
            ) throws IOException {
                setPermissions(dir);
                try (Stream<Path> stream = list(dir)) {
                    stream
                        .filter(Files::isDirectory)
                        .forEach(this::setPermissions);
                }
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e)
                throws IOException {
                if (e == null) {
                    delete(dir);
                }
                return super.postVisitDirectory(dir, e);
            }

            private void setPermissions(Path dir) {
                try {
                    setPosixFilePermissions(
                        dir,
                        EnumSet.allOf(PosixFilePermission.class)
                    );
                } catch (IOException ignored) {
                }
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
        return new Intent(ACTION_MAIN)
            .putExtra(EXTRA_DIRECTORY, dir.toString());
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
        Path dir = getExternalStorageDirectory().toPath().resolve(name);
        Path src = dir.resolve("z");
        Path dst = dir.resolve("Z");
        try {

            deleteRecursiveIfExists(dir);
            createFiles(src);

            assertTrue(exists(src, NOFOLLOW_LINKS));
            assertTrue(
                "Assuming the underlying file system is case insensitive",
                exists(dst, NOFOLLOW_LINKS)
            );

            move(src, dst);
            List<Path> actual;
            try (Stream<Path> stream = list(dir)) {
                actual = stream.collect(toList());
            }
            List<Path> expected = Collections.singletonList(dst);
            assertEquals(1, actual.size());
            if (!expected.equals(actual)) {
                throw new CannotRenameFileToDifferentCasing(
                    "\nexpected: " + expected + "\nactual: " + actual);
            }

            setActivityIntent(newIntent(dir));

        } catch (Throwable e) {
            try {
                deleteRecursiveIfExists(dir);
            } catch (Throwable sup) {
                e.addSuppressed(sup);
            }
            throw e;

        } finally {

            try {
                deleteIfExists(src);
            } catch (IOException ignore) {
            }

            try {
                deleteIfExists(dst);
            } catch (IOException ignore) {
            }

        }

        assertFalse(exists(src, NOFOLLOW_LINKS));
        assertFalse(exists(dst, NOFOLLOW_LINKS));

        return dir;
    }

}
