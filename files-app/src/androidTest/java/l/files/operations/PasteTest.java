package l.files.operations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import l.files.fs.Resource;
import l.files.fs.Resource.Stream;
import l.files.fs.local.ResourceBaseTest;

import static java.lang.Thread.currentThread;
import static java.util.Collections.singleton;

public abstract class PasteTest extends ResourceBaseTest {

    /**
     * When pasting emptying directories, they should be created on the
     * destination, even if they are empty.
     */
    public void testPastesEmptyDirectories() throws Exception {
        Resource src = dir1().resolve("empty").createDirectory();
        Resource dstDir = dir1().resolve("dst").createDirectory();
        create(singleton(src), dstDir).execute();
        assertTrue(dir1().resolve("dst/empty").exists());
    }

    /**
     * When pasting files into a directory with existing files with the same
     * names, the existing files should not be overridden, new files will be
     * pasted with new names.
     */
    public void testDoesNotOverrideExistingFile() throws Exception {
        List<Resource> sources = Arrays.<Resource>asList(
                dir1().resolve("a.txt").createFile(),
                dir1().resolve("b.mp4").createFile()
        );
        dir1().resolve("1/a.txt").createFile();
        dir1().resolve("1/b.mp4").createFile();

        Resource dstDir = dir1().resolve("1");

        create(sources, dstDir).execute();

        assertTrue(dir1().resolve("1/a.txt").exists());
        assertTrue(dir1().resolve("1/b.mp4").exists());
        assertTrue(dir1().resolve("1/a 2.txt").exists());
        assertTrue(dir1().resolve("1/b 2.mp4").exists());
    }

    /**
     * When pasting directories into a destination with existing directories
     * with the same names, the existing directories should not be overridden,
     * new directories will be pasted with new names.
     */
    public void testDoesNotOverrideExistingDirectory() throws Exception {
        dir1().resolve("a/1.txt").createFile();
        dir1().resolve("a/b/2.txt").createFile();
        dir1().resolve("a/b/3.txt").createFile();
        dir1().resolve("b/a/1.txt").createFile();
        Set<Resource> sources = Collections.<Resource>singleton(dir1().resolve("a"));
        Resource dstDir = dir1().resolve("b");

        create(sources, dstDir).execute();

        assertTrue(dir1().resolve("b/a/1.txt").exists());
        assertTrue(dir1().resolve("b/a 2/1.txt").exists());
        assertTrue(dir1().resolve("b/a 2/b/2.txt").exists());
        assertTrue(dir1().resolve("b/a 2/b/3.txt").exists());
    }

    public void testDoesNothingIfAlreadyCancelledOnExecution() throws Exception {
        final List<Resource> sources = Arrays.<Resource>asList(
                dir1().resolve("a/1.txt").createFile(),
                dir1().resolve("a/2.txt").createFile()
        );
        final Resource dstDir = dir1().resolve("b").createDirectory();

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
        try (Stream children = dstDir.openDirectory()) {
            assertFalse(children.iterator().hasNext());
        }
    }

    public void testErrorOnPastingSelfIntoSubDirectory() throws Exception {
        Resource parent = dir1().resolve("parent").createDirectory();
        Resource child = dir1().resolve("parent/child").createDirectory();
        try {
            create(singleton(parent), child).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    public void testErrorOnPastingIntoSelf() throws Exception {
        Resource dir = dir1().resolve("parent").createDirectory();
        try {
            create(singleton(dir), dir).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    abstract Paste create(Iterable<Resource> sources, Resource dstDir);

}
