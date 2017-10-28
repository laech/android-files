package l.files.operations;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class PasteTest extends PathBaseTest {

    /**
     * When pasting emptying directories, they should be created on the
     * destination, even if they are empty.
     */
    @Test
    public void pastesEmptyDirectories() throws Exception {
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
    @Test
    public void doesNotOverrideExistingFile() throws Exception {
        Set<Path> sources = new HashSet<>(asList(
            dir1().concat("a.txt").createFile(),
            dir1().concat("b.mp4").createFile()
        ));
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
    @Test
    public void doesNotOverrideExistingDirectory() throws Exception {
        Paths.createFiles(dir1().concat("a/1.txt"));
        Paths.createFiles(dir1().concat("a/b/2.txt"));
        Paths.createFiles(dir1().concat("a/b/3.txt"));
        Paths.createFiles(dir1().concat("b/a/1.txt"));
        Set<Path> sources = singleton(dir1().concat("a"));
        Path dstDir = dir1().concat("b");

        create(sources, dstDir).execute();

        assertTrue(dir1().concat("b/a/1.txt").exists(NOFOLLOW));
        assertTrue(dir1().concat("b/a 2/1.txt").exists(NOFOLLOW));
        assertTrue(dir1().concat("b/a 2/b/2.txt").exists(NOFOLLOW));
        assertTrue(dir1().concat("b/a 2/b/3.txt").exists(NOFOLLOW));
    }

    @Test
    public void doesNothingIfAlreadyCancelledOnExecution() throws Exception {
        final Set<Path> sources = new HashSet<>(asList(
                Paths.createFiles(dir1().concat("a/1.txt")),
                Paths.createFiles(dir1().concat("a/2.txt"))
        ));
        final Path dstDir = dir1().concat("b").createDirectory();

        Thread thread = new Thread(() -> {
            currentThread().interrupt();
            try {
                create(sources, dstDir).execute();
                fail();
            } catch (InterruptedException e) {
                // Pass
            } catch (FileException e) {
                fail();
            }
        });
        thread.start();
        thread.join();

        List<Path> actual = dstDir.list(new ArrayList<>());
        assertTrue(actual.isEmpty());
    }


    @Test
    public void errorOnPastingSelfIntoSubDirectory() throws Exception {
        Path parent = dir1().concat("parent").createDirectory();
        Path child = dir1().concat("parent/child").createDirectory();
        try {
            create(singleton(parent), child).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    @Test
    public void errorOnPastingIntoSelf() throws Exception {
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
