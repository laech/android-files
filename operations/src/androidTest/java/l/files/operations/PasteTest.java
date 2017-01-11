package l.files.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import l.files.fs.Path;
import l.files.fs.local.LocalFileSystem;
import l.files.testing.fs.PathBaseTest;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.testing.fs.Files.createFiles;

public abstract class PasteTest extends PathBaseTest {

    public PasteTest() {
        super(LocalFileSystem.INSTANCE);
    }

    /**
     * When pasting emptying directories, they should be created on the
     * destination, even if they are empty.
     */
    public void test_pastesEmptyDirectories() throws Exception {
        Path src = fs.createDir(dir1().concat("empty"));
        Path dstDir = fs.createDir(dir1().concat("dst"));
        create(singleton(src), dstDir).execute();
        assertTrue(fs.exists(dir1().concat("dst/empty"), NOFOLLOW));
    }

    /**
     * When pasting files into a directory with existing files with the same
     * names, the existing files should not be overridden, new files will be
     * pasted with new names.
     */
    public void test_doesNotOverrideExistingFile() throws Exception {
        List<Path> sources = asList(
                fs.createFile(dir1().concat("a.txt")),
                fs.createFile(dir1().concat("b.mp4"))
        );
        fs.createDir(dir1().concat("1"));
        fs.createFile(dir1().concat("1/a.txt"));
        fs.createFile(dir1().concat("1/b.mp4"));

        Path dstDir = dir1().concat("1");

        create(sources, dstDir).execute();

        assertTrue(fs.exists(dir1().concat("1/a.txt"), NOFOLLOW));
        assertTrue(fs.exists(dir1().concat("1/b.mp4"), NOFOLLOW));
        assertTrue(fs.exists(dir1().concat("1/a 2.txt"), NOFOLLOW));
        assertTrue(fs.exists(dir1().concat("1/b 2.mp4"), NOFOLLOW));
    }

    /**
     * When pasting directories into a destination with existing directories
     * with the same names, the existing directories should not be overridden,
     * new directories will be pasted with new names.
     */
    public void test_doesNotOverrideExistingDirectory() throws Exception {
        createFiles(fs, dir1().concat("a/1.txt"));
        createFiles(fs, dir1().concat("a/b/2.txt"));
        createFiles(fs, dir1().concat("a/b/3.txt"));
        createFiles(fs, dir1().concat("b/a/1.txt"));
        Set<Path> sources = singleton(dir1().concat("a"));
        Path dstDir = dir1().concat("b");

        create(sources, dstDir).execute();

        assertTrue(fs.exists(dir1().concat("b/a/1.txt"), NOFOLLOW));
        assertTrue(fs.exists(dir1().concat("b/a 2/1.txt"), NOFOLLOW));
        assertTrue(fs.exists(dir1().concat("b/a 2/b/2.txt"), NOFOLLOW));
        assertTrue(fs.exists(dir1().concat("b/a 2/b/3.txt"), NOFOLLOW));
    }

    public void test_doesNothingIfAlreadyCancelledOnExecution() throws Exception {
        final List<Path> sources = asList(
                createFiles(fs, dir1().concat("a/1.txt")),
                createFiles(fs, dir1().concat("a/2.txt"))
        );
        final Path dstDir = fs.createDir(dir1().concat("b"));

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

        List<Path> actual = fs.list(dstDir, NOFOLLOW, new ArrayList<Path>());
        assertTrue(actual.isEmpty());
    }

    public void test_errorOnPastingSelfIntoSubDirectory() throws Exception {
        Path parent = fs.createDir(dir1().concat("parent"));
        Path child = fs.createDir(dir1().concat("parent/child"));
        try {
            create(singleton(parent), child).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    public void test_errorOnPastingIntoSelf() throws Exception {
        Path dir = fs.createDir(dir1().concat("parent"));
        try {
            create(singleton(dir), dir).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    abstract Paste create(Collection<Path> sources, Path dstDir);

}
