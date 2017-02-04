package l.files.operations;

import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

import static java.lang.Thread.currentThread;
import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;

public abstract class PasteTest extends PathBaseTest {

    /**
     * When pasting emptying directories, they should be created on the
     * destination, even if they are empty.
     */
    public void test_pastesEmptyDirectories() throws Exception {
        Path src = dir1().concat("empty").createDirectory();
        Path dstDir = dir1().concat("dst").createDirectory();
        create(singleton(src), dstDir).execute();
        assertTrue(dir1().concat("dst/empty").exists(NOFOLLOW));
    }

    /**
     * When pasting files into a directory with existing files with the same
     * names, the existing files should not be overridden, new files will be
     * pasted with new names.
     */
    public void test_doesNotOverrideExistingFile() throws Exception {
        Set<Path> sources = ImmutableSet.<Path>of(
                dir1().concat("a.txt").createFile(),
                dir1().concat("b.mp4").createFile()
        );
        dir1().concat("1").createDirectory();
        dir1().concat("1/a.txt").createFile();
        dir1().concat("1/b.mp4").createFile();

        Path dstDir = dir1().concat("1");

        create(sources, dstDir).execute();

        assertTrue(dir1().concat("1/a.txt").exists(NOFOLLOW));
        assertTrue(dir1().concat("1/b.mp4").exists(NOFOLLOW));
        assertTrue(dir1().concat("1/a 2.txt").exists(NOFOLLOW));
        assertTrue(dir1().concat("1/b 2.mp4").exists(NOFOLLOW));
    }

    /**
     * When pasting directories into a destination with existing directories
     * with the same names, the existing directories should not be overridden,
     * new directories will be pasted with new names.
     */
    public void test_doesNotOverrideExistingDirectory() throws Exception {
        dir1().concat("a/1.txt").createFiles();
        dir1().concat("a/b/2.txt").createFiles();
        dir1().concat("a/b/3.txt").createFiles();
        dir1().concat("b/a/1.txt").createFiles();
        Set<Path> sources = Collections.<Path>singleton(dir1().concat("a"));
        Path dstDir = dir1().concat("b");

        create(sources, dstDir).execute();

        assertTrue(dir1().concat("b/a/1.txt").exists(NOFOLLOW));
        assertTrue(dir1().concat("b/a 2/1.txt").exists(NOFOLLOW));
        assertTrue(dir1().concat("b/a 2/b/2.txt").exists(NOFOLLOW));
        assertTrue(dir1().concat("b/a 2/b/3.txt").exists(NOFOLLOW));
    }

    public void test_doesNothingIfAlreadyCancelledOnExecution() throws Exception {
        final Set<Path> sources = ImmutableSet.<Path>of(
                dir1().concat("a/1.txt").createFiles(),
                dir1().concat("a/2.txt").createFiles()
        );
        final Path dstDir = dir1().concat("b").createDirectory();

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

        List<Path> actual = dstDir.list(NOFOLLOW, new ArrayList<Path>());
        assertTrue(actual.isEmpty());
    }

    public void test_errorOnPastingSelfIntoSubDirectory() throws Exception {
        Path parent = dir1().concat("parent").createDirectory();
        Path child = dir1().concat("parent/child").createDirectory();
        try {
            create(singleton(parent), child).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    public void test_errorOnPastingIntoSelf() throws Exception {
        Path dir = dir1().concat("parent").createDirectory();
        try {
            create(singleton(dir), dir).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    abstract Paste create(
            Set<? extends Path> sourcePaths,
            Path destinationDir
    );

}
