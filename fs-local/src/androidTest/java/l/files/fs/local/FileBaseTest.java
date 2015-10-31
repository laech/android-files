package l.files.fs.local;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.EnumSet;

import l.files.fs.File;
import l.files.fs.Permission;
import l.files.fs.Visitor;
import l.files.testing.BaseTest;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;

public abstract class FileBaseTest extends BaseTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private File dir1;
    private File dir2;

    protected final File dir1() {
        if (dir1 == null) {
            try {
                dir1 = LocalFile.of(folder.newFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir1;
    }

    protected final File dir2() {
        if (dir2 == null) {
            try {
                dir2 = LocalFile.of(folder.newFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir2;
    }

    @Override
    public void tearDown() throws Exception {
        delete(dir1);
        delete(dir2);
        super.tearDown();
    }

    private static void delete(File file) throws IOException {
        if (file == null) {
            return;
        }
        file.traverse(
                NOFOLLOW,
                new Visitor.Base() {

                    @Override
                    public Result onPreVisit(File file) throws IOException {
                        try {
                            file.setPermissions(EnumSet.allOf(Permission.class));
                        } catch (IOException ignore) {
                        }
                        return CONTINUE;
                    }

                    @Override
                    public Result onPostVisit(File file) throws IOException {
                        file.delete();
                        return CONTINUE;
                    }

                });
    }

}
