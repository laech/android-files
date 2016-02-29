package l.files.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.fs.Files.createDir;
import static l.files.fs.Files.createFile;
import static l.files.fs.Files.createFiles;
import static l.files.fs.Files.exists;
import static l.files.fs.Files.list;
import static l.files.fs.LinkOption.NOFOLLOW;

public abstract class PasteTest extends PathBaseTest {

    /**
     * When pasting emptying directories, they should be created on the
     * destination, even if they are empty.
     */
    public void test_pastesEmptyDirectories() throws Exception {
        Path src = createDir(dir1().resolve("empty"));
        Path dstDir = createDir(dir1().resolve("dst"));
        create(singleton(src), dstDir).execute();
        assertTrue(exists(dir1().resolve("dst/empty"), NOFOLLOW));
    }

    /**
     * When pasting files into a directory with existing files with the same
     * names, the existing files should not be overridden, new files will be
     * pasted with new names.
     */
    public void test_doesNotOverrideExistingFile() throws Exception {
        List<Path> sources = asList(
                createFile(dir1().resolve("a.txt")),
                createFile(dir1().resolve("b.mp4"))
        );
        createDir(dir1().resolve("1"));
        createFile(dir1().resolve("1/a.txt"));
        createFile(dir1().resolve("1/b.mp4"));

        Path dstDir = dir1().resolve("1");

        create(sources, dstDir).execute();

        assertTrue(exists(dir1().resolve("1/a.txt"), NOFOLLOW));
        assertTrue(exists(dir1().resolve("1/b.mp4"), NOFOLLOW));
        assertTrue(exists(dir1().resolve("1/a 2.txt"), NOFOLLOW));
        assertTrue(exists(dir1().resolve("1/b 2.mp4"), NOFOLLOW));
    }

    /**
     * When pasting directories into a destination with existing directories
     * with the same names, the existing directories should not be overridden,
     * new directories will be pasted with new names.
     */
    public void test_doesNotOverrideExistingDirectory() throws Exception {
        createFiles(dir1().resolve("a/1.txt"));
        createFiles(dir1().resolve("a/b/2.txt"));
        createFiles(dir1().resolve("a/b/3.txt"));
        createFiles(dir1().resolve("b/a/1.txt"));
        Set<Path> sources = singleton(dir1().resolve("a"));
        Path dstDir = dir1().resolve("b");

        create(sources, dstDir).execute();

        assertTrue(exists(dir1().resolve("b/a/1.txt"), NOFOLLOW));
        assertTrue(exists(dir1().resolve("b/a 2/1.txt"), NOFOLLOW));
        assertTrue(exists(dir1().resolve("b/a 2/b/2.txt"), NOFOLLOW));
        assertTrue(exists(dir1().resolve("b/a 2/b/3.txt"), NOFOLLOW));
    }

    public void test_doesNothingIfAlreadyCancelledOnExecution() throws Exception {
        final List<Path> sources = asList(
                createFiles(dir1().resolve("a/1.txt")),
                createFiles(dir1().resolve("a/2.txt"))
        );
        final Path dstDir = createDir(dir1().resolve("b"));

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                currentThread().interrupt();
                try {
                    create(sources, dstDir).execute();
                    fail();
                } catch (InterruptedException e) {
                    // Pass
                } catch (FileException e) {
                    fail();
                }
            }
        });
        thread.start();
        thread.join();

        List<Path> actual = list(dstDir, NOFOLLOW, new ArrayList<Path>());
        assertTrue(actual.isEmpty());
    }

    public void test_errorOnPastingSelfIntoSubDirectory() throws Exception {
        Path parent = createDir(dir1().resolve("parent"));
        Path child = createDir(dir1().resolve("parent/child"));
        try {
            create(singleton(parent), child).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    public void test_errorOnPastingIntoSelf() throws Exception {
        Path dir = createDir(dir1().resolve("parent"));
        try {
            create(singleton(dir), dir).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    abstract Paste create(Collection<Path> sources, Path dstDir);

}
