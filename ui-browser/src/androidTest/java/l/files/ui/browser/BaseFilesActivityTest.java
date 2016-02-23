package l.files.ui.browser;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import l.files.base.Provider;
import l.files.base.Throwables;
import l.files.fs.Files;
import l.files.fs.Name;
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

public class BaseFilesActivityTest extends ActivityInstrumentationTestCase2<FilesActivity> {

    private Path dir;
    private UiFileActivity screen;

    public BaseFilesActivityTest() {
        super(FilesActivity.class);
    }

    private File createTempFolder() throws IOException {
        File dir = File.createTempFile(getClass().getSimpleName(), null);
        assertTrue(dir.delete());
        assertTrue(dir.mkdir());
        return dir;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dir = Paths.get(createTempFolder());
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

    @Override
    protected void tearDown() throws Exception {
        screen = null;
        if (Files.exists(dir, NOFOLLOW)) {
            Files.traverse(dir, NOFOLLOW, delete());
        }
        dir = null;
        super.tearDown();
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

    Path createCaseInsensitiveFileSystemDir(String name) throws IOException {

        /*
         * Use getExternalStorageDirectory() for this test, as this issue
         * is only reproducible there (this test assumes this, making it
         * less reliable).
         *
         * The bug: "a" gets renamed to "A", instead of displaying only
         * "A", both "a" and "A" are displayed.
         */
        Path dir = Paths.get(getExternalStorageDirectory()).resolve(name);
        Path src = dir.resolve("z");
        Path dst = dir.resolve("Z");
        try {

            Files.deleteRecursiveIfExists(dir);
            Files.createFiles(src);

            assertTrue(Files.exists(src, NOFOLLOW));
            assertTrue(
                    "Assuming the underlying file system is case insensitive",
                    Files.exists(dst, NOFOLLOW));

            Files.move(src, dst);
            List<Name> actual = Files.list(dir, NOFOLLOW, new ArrayList<Name>());
            assertEquals(1, actual.size());
            assertEquals(
                    "Assuming the file can be renamed to different casing",
                    singletonList(dst.name()),
                    actual);

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
