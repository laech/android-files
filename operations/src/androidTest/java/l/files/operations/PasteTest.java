package l.files.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import l.files.fs.Name;
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
        Name sourceFile = createDir(dir1().resolve("empty")).name();
        Path destinationDirectory = createDir(dir1().resolve("dst"));
        create(dir1(), singleton(sourceFile), destinationDirectory).execute();
        assertTrue(exists(dir1().resolve("dst/empty"), NOFOLLOW));
    }

    /**
     * When pasting files into a directory with existing files with the same
     * names, the existing files should not be overridden, new files will be
     * pasted with new names.
     */
    public void test_doesNotOverrideExistingFile() throws Exception {
        List<Name> sourceFiles = asList(
                createFile(dir1().resolve("a.txt")).name(),
                createFile(dir1().resolve("b.mp4")).name()
        );
        createDir(dir1().resolve("1"));
        createFile(dir1().resolve("1/a.txt"));
        createFile(dir1().resolve("1/b.mp4"));

        Path dstDir = dir1().resolve("1");

        create(dir1(), sourceFiles, dstDir).execute();

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
        Set<Name> sourceFiles = singleton(dir1().resolve("a").name());
        Path dstDir = dir1().resolve("b");

        create(dir1(), sourceFiles, dstDir).execute();

        assertTrue(exists(dir1().resolve("b/a/1.txt"), NOFOLLOW));
        assertTrue(exists(dir1().resolve("b/a 2/1.txt"), NOFOLLOW));
        assertTrue(exists(dir1().resolve("b/a 2/b/2.txt"), NOFOLLOW));
        assertTrue(exists(dir1().resolve("b/a 2/b/3.txt"), NOFOLLOW));
    }

    public void test_doesNothingIfAlreadyCancelledOnExecution() throws Exception {
        final Path sourceDirectory = dir1().resolve("a");
        final List<Name> sourceFiles = asList(
                createFiles(sourceDirectory.resolve("1.txt")).name(),
                createFiles(sourceDirectory.resolve("2.txt")).name()
        );
        final Path destinationDirectory = createDir(dir1().resolve("b"));

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                currentThread().interrupt();
                try {
                    create(dir1().resolve("a"), sourceFiles, destinationDirectory).execute();
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

        List<Name> actual = list(destinationDirectory, NOFOLLOW, new ArrayList<Name>());
        assertTrue(actual.isEmpty());
    }

    public void test_errorOnPastingSelfIntoSubDirectory() throws Exception {
        Path parent = createDir(dir1().resolve("parent"));
        Path child = createDir(dir1().resolve("parent/child"));
        try {
            create(parent.parent(), singleton(parent.name()), child).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    public void test_errorOnPastingIntoSelf() throws Exception {
        Path dir = createDir(dir1().resolve("parent"));
        try {
            create(dir.parent(), singleton(dir.name()), dir).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    abstract Paste create(
            Path sourceDirectory,
            Collection<Name> sourceFiles,
            Path destinationDirectory);

}
