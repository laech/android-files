package l.files.operations;

import l.files.testing.fs.PathBaseTest;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.Thread.currentThread;
import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.testing.fs.Paths.createFiles;
import static org.junit.Assert.*;

public abstract class PasteTest extends PathBaseTest {

    /**
     * When pasting emptying directories, they should be created on the
     * destination, even if they are empty.
     */
    @Test
    public void pastesEmptyDirectories() throws Exception {
        Path src = createDirectory(dir1().resolve("empty"));
        Path dstDir = createDirectory(dir1().resolve("dst"));
        create(singleton(src), dstDir).execute();
        assertTrue(exists(dir1().resolve("dst/empty"), NOFOLLOW_LINKS));
    }

    /**
     * When pasting files into a directory with existing files with the same
     * names, the existing files should not be overridden, new files will be
     * pasted with new names.
     */
    @Test
    public void doesNotOverrideExistingFile() throws Exception {
        Set<Path> sources = new HashSet<>(asList(
            createFile(dir1().resolve("a.txt")),
            createFile(dir1().resolve("b.mp4"))
        ));
        createDirectory(dir1().resolve("1"));
        createFile(dir1().resolve("1/a.txt"));
        createFile(dir1().resolve("1/b.mp4"));

        Path dstDir = dir1().resolve("1");

        create(sources, dstDir).execute();

        assertTrue(exists(dir1().resolve("1/a.txt"), NOFOLLOW_LINKS));
        assertTrue(exists(dir1().resolve("1/b.mp4"), NOFOLLOW_LINKS));
        assertTrue(exists(dir1().resolve("1/a 2.txt"), NOFOLLOW_LINKS));
        assertTrue(exists(dir1().resolve("1/b 2.mp4"), NOFOLLOW_LINKS));
    }

    /**
     * When pasting directories into a destination with existing directories
     * with the same names, the existing directories should not be overridden,
     * new directories will be pasted with new names.
     */
    @Test
    public void doesNotOverrideExistingDirectory() throws Exception {
        createFiles(dir1().resolve("a/1.txt"));
        createFiles(dir1().resolve("a/b/2.txt"));
        createFiles(dir1().resolve("a/b/3.txt"));
        createFiles(dir1().resolve("b/a/1.txt"));
        Set<Path> sources = singleton(dir1().resolve("a"));
        Path dstDir = dir1().resolve("b");

        create(sources, dstDir).execute();

        assertTrue(exists(dir1().resolve("b/a/1.txt"), NOFOLLOW_LINKS));
        assertTrue(exists(dir1().resolve("b/a 2/1.txt"), NOFOLLOW_LINKS));
        assertTrue(exists(dir1().resolve("b/a 2/b/2.txt"), NOFOLLOW_LINKS));
        assertTrue(exists(dir1().resolve("b/a 2/b/3.txt"), NOFOLLOW_LINKS));
    }

    @Test
    public void doesNothingIfAlreadyCancelledOnExecution() throws Exception {
        Set<Path> sources = new HashSet<>(asList(
            createFiles(dir1().resolve("a/1.txt")),
            createFiles(dir1().resolve("a/2.txt"))
        ));
        Path dstDir = createDirectory(dir1().resolve("b"));

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

        try (Stream<Path> stream = Files.list(dstDir)) {
            assertEquals(0, stream.count());
        }
    }


    @Test
    public void errorOnPastingSelfIntoSubDirectory() throws Exception {
        Path parent = createDirectory(dir1().resolve("parent"));
        Path child =
            createDirectory(dir1().resolve("parent/child"));
        try {
            create(singleton(parent), child).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    @Test
    public void errorOnPastingIntoSelf() throws Exception {
        Path dir = createDirectory(dir1().resolve("parent"));
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
