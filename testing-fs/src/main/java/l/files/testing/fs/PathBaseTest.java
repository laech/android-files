package l.files.testing.fs;

import androidx.annotation.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.attribute.PosixFilePermission.*;

public abstract class PathBaseTest {

    @Rule
    public final TestName testName = new TestName();

    @Nullable
    private Path dir1;

    @Nullable
    private Path dir2;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        if (dir1 != null) {
            deleteRecursive(dir1);
        }
        if (dir2 != null) {
            deleteRecursive(dir2);
        }
    }

    private void deleteRecursive(Path path) throws IOException {
        if (!exists(path, NOFOLLOW_LINKS)) {
            return;
        }
        walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(
                Path dir,
                BasicFileAttributes attrs
            ) throws IOException {
                setPosixFilePermissions(dir, EnumSet.of(
                    OWNER_READ, OWNER_WRITE, OWNER_EXECUTE));
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                throws IOException {
                delete(dir);
                return super.postVisitDirectory(dir, exc);
            }

            @Override
            public FileVisitResult visitFile(
                Path file,
                BasicFileAttributes attrs
            ) throws IOException {
                delete(file);
                return super.visitFile(file, attrs);
            }
        });
    }

    protected Path dir1() {
        if (dir1 == null) {
            try {
                dir1 = createTempFolder();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return dir1;
    }

    protected Path dir2() {
        if (dir2 == null) {
            try {
                dir2 = createTempFolder();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return dir2;
    }

    private Path createTempFolder() throws IOException {
        return Files.createTempDirectory(
            getClass().getSimpleName() + "." + testName.getMethodName());
    }

}
