package l.files.ui.browser;

import android.content.Intent;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Permission;
import l.files.fs.Visitor;
import l.files.fs.local.LocalFile;
import l.files.testing.BaseActivityTest;

import static java.io.File.createTempFile;
import static java.lang.System.currentTimeMillis;
import static l.files.fs.Files.traverse;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.ui.browser.FilesActivity.EXTRA_DIRECTORY;

public class BaseFilesActivityTest extends BaseActivityTest<FilesActivity> {

    private File dir;
    private UiFileActivity screen;

    public BaseFilesActivityTest() {
        super(FilesActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dir = LocalFile.of(createTempDir());
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

    private java.io.File createTempDir() throws IOException {
        java.io.File file = createTempFile("tmp", String.valueOf(currentTimeMillis()));
        assertTrue(file.delete());
        assertTrue(file.mkdir());
        return file;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        screen = null;
        if (dir.exists(NOFOLLOW)) {
            traverse(dir, NOFOLLOW, delete());
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

    protected UiFileActivity screen() {
        getActivity();
        return screen;
    }

    protected File dir() {
        return dir;
    }

    private Intent newIntent(File dir) {
        return new Intent().putExtra(EXTRA_DIRECTORY, dir);
    }
}
