package l.files.operations;

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import l.files.fs.File;
import l.files.fs.Stream;
import l.files.testing.fs.FileBaseTest;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class PasteTest extends FileBaseTest {

    /**
     * When pasting emptying directories, they should be created on the
     * destination, even if they are empty.
     */
    @Test
    public void pastesEmptyDirectories() throws Exception {
        File src = dir1().resolve("empty").createDir();
        File dstDir = dir1().resolve("dst").createDir();
        create(singleton(src), dstDir).execute();
        assertTrue(dir1().resolve("dst/empty").exists(NOFOLLOW));
    }

    /**
     * When pasting files into a directory with existing files with the same
     * names, the existing files should not be overridden, new files will be
     * pasted with new names.
     */
    @Test
    public void doesNotOverrideExistingFile() throws Exception {
        List<File> sources = asList(
                dir1().resolve("a.txt").createFile(),
                dir1().resolve("b.mp4").createFile()
        );
        dir1().resolve("1").createDir();
        dir1().resolve("1/a.txt").createFile();
        dir1().resolve("1/b.mp4").createFile();

        File dstDir = dir1().resolve("1");

        create(sources, dstDir).execute();

        assertTrue(dir1().resolve("1/a.txt").exists(NOFOLLOW));
        assertTrue(dir1().resolve("1/b.mp4").exists(NOFOLLOW));
        assertTrue(dir1().resolve("1/a 2.txt").exists(NOFOLLOW));
        assertTrue(dir1().resolve("1/b 2.mp4").exists(NOFOLLOW));
    }

    /**
     * When pasting directories into a destination with existing directories
     * with the same names, the existing directories should not be overridden,
     * new directories will be pasted with new names.
     */
    @Test
    public void doesNotOverrideExistingDirectory() throws Exception {
        dir1().resolve("a/1.txt").createFiles();
        dir1().resolve("a/b/2.txt").createFiles();
        dir1().resolve("a/b/3.txt").createFiles();
        dir1().resolve("b/a/1.txt").createFiles();
        Set<File> sources = Collections.singleton(dir1().resolve("a"));
        File dstDir = dir1().resolve("b");

        create(sources, dstDir).execute();

        assertTrue(dir1().resolve("b/a/1.txt").exists(NOFOLLOW));
        assertTrue(dir1().resolve("b/a 2/1.txt").exists(NOFOLLOW));
        assertTrue(dir1().resolve("b/a 2/b/2.txt").exists(NOFOLLOW));
        assertTrue(dir1().resolve("b/a 2/b/3.txt").exists(NOFOLLOW));
    }

    @Test
    public void doesNothingIfAlreadyCancelledOnExecution() throws Exception {
        final List<File> sources = asList(
                dir1().resolve("a/1.txt").createFiles(),
                dir1().resolve("a/2.txt").createFiles()
        );
        final File dstDir = dir1().resolve("b").createDir();

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
        try (Stream<File> stream = dstDir.list(NOFOLLOW)) {
            assertFalse(stream.iterator().hasNext());
        }
    }

    @Test
    public void errorOnPastingSelfIntoSubDirectory() throws Exception {
        File parent = dir1().resolve("parent").createDir();
        File child = dir1().resolve("parent/child").createDir();
        try {
            create(singleton(parent), child).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    @Test
    public void errorOnPastingIntoSelf() throws Exception {
        File dir = dir1().resolve("parent").createDir();
        try {
            create(singleton(dir), dir).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    abstract Paste create(Collection<File> sources, File dstDir);

}
