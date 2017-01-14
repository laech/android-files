package l.files.operations;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import l.files.fs.FileSystem;
import l.files.fs.Path;
import l.files.fs.local.LocalFileSystem;
import l.files.testing.fs.PathBaseTest;

import static java.lang.Thread.currentThread;
import static java.util.Collections.singletonMap;
import static l.files.fs.LinkOption.NOFOLLOW;

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
        create(singletonMap(src, fs), fs, dstDir).execute();
        assertTrue(fs.exists(dir1().concat("dst/empty"), NOFOLLOW));
    }

    /**
     * When pasting files into a directory with existing files with the same
     * names, the existing files should not be overridden, new files will be
     * pasted with new names.
     */
    public void test_doesNotOverrideExistingFile() throws Exception {
        Map<Path, FileSystem> sources = ImmutableMap.<Path, FileSystem>of(
                fs.createFile(dir1().concat("a.txt")), fs,
                fs.createFile(dir1().concat("b.mp4")), fs
        );
        fs.createDir(dir1().concat("1"));
        fs.createFile(dir1().concat("1/a.txt"));
        fs.createFile(dir1().concat("1/b.mp4"));

        Path dstDir = dir1().concat("1");

        create(sources, fs, dstDir).execute();

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
        fs.createFiles(dir1().concat("a/1.txt"));
        fs.createFiles(dir1().concat("a/b/2.txt"));
        fs.createFiles(dir1().concat("a/b/3.txt"));
        fs.createFiles(dir1().concat("b/a/1.txt"));
        Map<Path, FileSystem> sources = Collections.<Path, FileSystem>
                singletonMap(dir1().concat("a"), fs);
        Path dstDir = dir1().concat("b");

        create(sources, fs, dstDir).execute();

        assertTrue(fs.exists(dir1().concat("b/a/1.txt"), NOFOLLOW));
        assertTrue(fs.exists(dir1().concat("b/a 2/1.txt"), NOFOLLOW));
        assertTrue(fs.exists(dir1().concat("b/a 2/b/2.txt"), NOFOLLOW));
        assertTrue(fs.exists(dir1().concat("b/a 2/b/3.txt"), NOFOLLOW));
    }

    public void test_doesNothingIfAlreadyCancelledOnExecution() throws Exception {
        final Map<Path, FileSystem> sources = ImmutableMap.<Path, FileSystem>of(
                fs.createFiles(dir1().concat("a/1.txt")), fs,
                fs.createFiles(dir1().concat("a/2.txt")), fs
        );
        final Path dstDir = fs.createDir(dir1().concat("b"));

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                currentThread().interrupt();
                try {
                    create(sources, fs, dstDir).execute();
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
            create(singletonMap(parent, fs), fs, child).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    public void test_errorOnPastingIntoSelf() throws Exception {
        Path dir = fs.createDir(dir1().concat("parent"));
        try {
            create(singletonMap(dir, fs), fs, dir).execute();
            fail();
        } catch (CannotPasteIntoSelfException pass) {
            // Pass
        }
    }

    abstract Paste create(
            Map<? extends Path, ? extends FileSystem> sourcePaths,
            FileSystem destinationFs,
            Path destinationDir
    );

}
