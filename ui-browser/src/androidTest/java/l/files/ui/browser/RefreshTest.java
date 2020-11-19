package l.files.ui.browser;

import l.files.testing.fs.Paths;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.ui.browser.FilesLoader.BATCH_UPDATE_MILLIS;
import static l.files.ui.browser.sort.FileSort.MODIFIED;
import static org.junit.Assert.assertFalse;

public final class RefreshTest extends BaseFilesActivityTest {

    @Test
    public void manual_refresh_updates_outdated_files()
        throws Throwable {

        int watchLimit = 5;
        setActivityIntent(newIntent(dir(), watchLimit));

        for (int i = 0; i < watchLimit + 5; i++) {
            createDirectory(dir().resolve(String.valueOf(i)));
        }

        screen()
            .sort()
            .by(MODIFIED)
            .assertListMatchesFileSystem(dir())
            .assertRefreshMenuVisible(true);

        testRefreshInManualMode(dir());
        testFileCreationDeletionWillStillBeNotifiedInManualMode(dir());
    }

    private void testRefreshInManualMode(Path dir)
        throws Throwable {

        // Currently Files::getLastModifiedTime only has second precision,
        // so need to wait for a bit until it changes
        Thread.sleep(1500);

        try (Stream<Path> stream = list(dir)) {
            Path childDir = stream.filter(Files::isDirectory)
                .findFirst()
                .orElseThrow(NoSuchElementException::new);

            try {
                createFile(childDir.resolve("x"));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        boolean updated = false;
        try {
            screen().assertListMatchesFileSystem(
                dir,
                BATCH_UPDATE_MILLIS + 2000,
                MILLISECONDS
            );
            updated = true;
        } catch (AssertionError e) {
            // Pass
        }
        assertFalse("Expected auto refresh to have been disabled", updated);
        screen().refresh().assertListMatchesFileSystem(dir);
    }

    private void testFileCreationDeletionWillStillBeNotifiedInManualMode(Path dir)
        throws IOException {

        createFile(dir.resolve("file-" + nanoTime()));
        createDirectory(dir.resolve("dir-" + nanoTime()));
        move(
            createFile(dir.resolve("before-move-" + nanoTime())),
            dir.resolve("after-move-" + nanoTime())
        );

        try (Stream<Path> stream = list(dir)) {
            stream.findFirst().ifPresent(file -> {
                try {
                    Paths.deleteRecursive(l.files.fs.Path.of(file));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }

        screen().assertListMatchesFileSystem(dir);
    }

    @Test
    public void auto_detect_files_added_and_removed_while_loading()
        throws Exception {

        for (int i = 0; i < 10; i++) {
            createDirectory(dir().resolve(String.valueOf(i)));
        }

        Thread thread = new Thread(() -> {
            long start = currentTimeMillis();
            while (currentTimeMillis() - start < 5000) {
                try {
                    deleteFiles(2);
                    createDirectory(randomFile(dir()));
                    createFile(randomFile(dir()));
                } catch (IOException ignore) {
                }
            }
        });
        thread.start();

        screen();
        thread.join();
        screen().assertListMatchesFileSystem(dir());
    }

    private void deleteFiles(int n) throws IOException {
        try (Stream<Path> stream = list(dir())) {
            stream.limit(n).forEach(file -> {
                try {
                    Paths.deleteRecursive(l.files.fs.Path.of(file));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    private Path randomFile(Path dir) {
        return dir.resolve(String.valueOf(Math.random()));
    }

    @Test
    public void auto_show_correct_information_on_large_change_events()
        throws Exception {
        createFile(dir().resolve("a"));
        screen().assertListMatchesFileSystem(dir());

        long end = currentTimeMillis() + SECONDS.toMillis(5);
        while (currentTimeMillis() < end) {
            updatePermissions("a");
            updateFileContent("b");
            updateDirectoryChild("c");
            updateLink("d", "a", "b");
            updateDirectory("e");
            updateAttributes();
        }

        screen().assertListMatchesFileSystem(dir());
    }

    private void updateAttributes() throws IOException {

        Random r = new Random();
        try (Stream<Path> stream = list(dir())) {
            stream.forEach(child -> {
                try {
                    setLastModifiedTime(
                        child,
                        FileTime.from(Instant.ofEpochSecond(
                            r.nextInt((int) (currentTimeMillis() / 1000)),
                            r.nextInt(999999)
                        ))
                    );
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    private void updateDirectory(String name) throws IOException {
        Path dir = dir().resolve(name);
        if (exists(dir, NOFOLLOW_LINKS)) {
            delete(dir);
        } else {
            createDirectory(dir);
        }
    }

    private void updatePermissions(String name) throws IOException {
        Path res = dir().resolve(name);
        Paths.createFiles(l.files.fs.Path.of(res));
        if (isReadable(res)) {
            setPosixFilePermissions(
                res,
                PosixFilePermissions.fromString("r--r--r--")
            );
        } else {
            setPosixFilePermissions(res, emptySet());
        }
    }

    private void updateFileContent(String name) throws IOException {
        Path file = dir().resolve(name);
        Paths.createFiles(l.files.fs.Path.of(file));
        write(file, singleton(String.valueOf(new Random().nextLong())));
    }

    private void updateDirectoryChild(String name) throws IOException {
        Path dir = createDirectories(dir().resolve(name));
        Path child = dir.resolve("child");
        if (exists(child, NOFOLLOW_LINKS)) {
            delete(child);
        } else {
            createFile(child);
        }
    }

    private void updateLink(String name, String target1, String target2)
        throws IOException {
        Path link = dir().resolve(name);
        if (exists(link, NOFOLLOW_LINKS)) {
            delete(link);
        }
        createSymbolicLink(
            link,
            new Random().nextInt() % 2 == 0
                ? dir().resolve(target1)
                : dir().resolve(target2)
        );
    }
}
