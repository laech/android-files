package l.files.fs.local;

import android.text.TextUtils;

import com.google.auto.value.AutoValue;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.TraversalCallback;

import static java.util.Arrays.asList;
import static l.files.fs.Files.createDir;
import static l.files.fs.Files.createDirs;
import static l.files.fs.Files.createFile;
import static l.files.fs.Files.createSymbolicLink;
import static l.files.fs.Files.setPermissions;
import static l.files.fs.Files.traverse;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.TraversalCallback.Result.CONTINUE;
import static l.files.fs.TraversalCallback.Result.SKIP;
import static l.files.fs.TraversalCallback.Result.TERMINATE;
import static l.files.fs.local.LocalFileTraverseTest.TraversalOrder.POST;
import static l.files.fs.local.LocalFileTraverseTest.TraversalOrder.PRE;

public final class LocalFileTraverseTest extends PathBaseTest {

    private static final Comparator<Path> SORT_BY_NAME = new Comparator<Path>() {
        @Override
        public int compare(Path a, Path b) {
            return a.name().toString().compareTo(b.name().toString());
        }
    };

    @Test
    public void traverse_noFollowLink() throws Exception {
        Path dir = createDir(dir1().resolve("dir"));
        Path link = createSymbolicLink(dir1().resolve("link"), dir);
        createFile(link.resolve("a"));
        createFile(link.resolve("b"));

        Recorder recorder = new Recorder();
        traverse(link, NOFOLLOW, recorder);
        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, link),
                TraversalEvent.of(POST, link)
        );
        checkEquals(expected, recorder.events);
    }

    @Test
    public void traverse_followLink_rootOnly() throws Exception {
        createDir(dir1().resolve("dir"));
        createFile(dir1().resolve("dir/a"));
        createDir(dir1().resolve("dir/b"));
        createFile(dir1().resolve("dir/b/1"));
        createSymbolicLink(dir1().resolve("dir/c"), dir1().resolve("dir/b"));
        createSymbolicLink(dir1().resolve("link"), dir1().resolve("dir"));

        Recorder recorder = new Recorder();
        traverse(dir1().resolve("link"), FOLLOW, recorder, SORT_BY_NAME);
        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1().resolve("link")),
                TraversalEvent.of(PRE, dir1().resolve("link/a")),
                TraversalEvent.of(POST, dir1().resolve("link/a")),
                TraversalEvent.of(PRE, dir1().resolve("link/b")),
                TraversalEvent.of(PRE, dir1().resolve("link/b/1")),
                TraversalEvent.of(POST, dir1().resolve("link/b/1")),
                TraversalEvent.of(POST, dir1().resolve("link/b")),
                TraversalEvent.of(PRE, dir1().resolve("link/c")),
                // link/c is not followed into
                TraversalEvent.of(POST, dir1().resolve("link/c")),
                TraversalEvent.of(POST, dir1().resolve("link"))
        );
        checkEquals(expected, recorder.events);
    }

    @Test
    public void traverse_followLink() throws Exception {
        Path dir = createDir(dir1().resolve("dir"));
        Path link = createSymbolicLink(dir1().resolve("link"), dir);
        Path a = createFile(link.resolve("a"));

        Recorder recorder = new Recorder();
        traverse(link, FOLLOW, recorder);
        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, link),
                TraversalEvent.of(PRE, a),
                TraversalEvent.of(POST, a),
                TraversalEvent.of(POST, link)
        );
        checkEquals(expected, recorder.events);
    }

    @Test
    public void traverse_continuesIfExceptionHandlerDoesNotThrow_pre() throws Exception {
        createDir(dir1().resolve("a"));
        createDir(dir1().resolve("b"));

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1())
        );

        Recorder recorder = new Recorder() {

            @Override
            public Result onPreVisit(Path file) throws IOException {
                if (file.name().toString().equals("a")) {
                    throw new IOException("Test");
                }
                return super.onPreVisit(file);
            }

            @Override
            public void onException(Path file, IOException e) throws IOException {
                // Ignore
            }

        };
        traverse(dir1(), NOFOLLOW, recorder);

        checkEquals(expected, recorder.events);
    }

    @Test
    public void traverse_continuesIfExceptionHandlerDoesNotThrow_post() throws Exception {
        createDirs(dir1().resolve("a/1"));
        createFile(dir1().resolve("a/1/i"));
        createDirs(dir1().resolve("b"));

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().resolve("a")),
                TraversalEvent.of(PRE, dir1().resolve("a/1")),
                TraversalEvent.of(PRE, dir1().resolve("a/1/i")),
                TraversalEvent.of(POST, dir1().resolve("a/1/i")),
                TraversalEvent.of(POST, dir1().resolve("a/1")),
                TraversalEvent.of(POST, dir1().resolve("a")),
                TraversalEvent.of(PRE, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1())
        );

        Recorder recorder = new Recorder() {

            @Override
            public Result onPostVisit(Path file) throws IOException {
                super.onPostVisit(file);
                if (file.name().toString().equals("1")) {
                    throw new IOException("Test");
                }
                return CONTINUE;
            }

            @Override
            public void onException(Path file, IOException e) throws IOException {
                // Ignore
            }

        };
        traverse(dir1(), NOFOLLOW, recorder, SORT_BY_NAME);

        checkEquals(expected, recorder.events);
    }

    @Test
    public void traverse_continuesIfExceptionHandlerDoesNotThrow_noPermission() throws Exception {
        createDirs(dir1().resolve("a/1"));
        createFile(dir1().resolve("a/1/i"));
        createDirs(dir1().resolve("a/2"));
        createDirs(dir1().resolve("b"));
        setPermissions(dir1().resolve("a"), Permission.none());

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().resolve("a")),
                TraversalEvent.of(POST, dir1().resolve("a")),
                TraversalEvent.of(PRE, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1())
        );

        Recorder recorder = new Recorder() {
            @Override
            public void onException(Path file, IOException e) {
                // Ignore
            }
        };
        traverse(dir1(), NOFOLLOW, recorder, SORT_BY_NAME);

        checkEquals(expected, recorder.events);
    }

    @Test
    public void traverse_order() throws Exception {
        createDirs(dir1().resolve("a/1"));
        createFile(dir1().resolve("a/1/i"));
        createDirs(dir1().resolve("a/2"));
        createDirs(dir1().resolve("b"));

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().resolve("a")),
                TraversalEvent.of(PRE, dir1().resolve("a/1")),
                TraversalEvent.of(PRE, dir1().resolve("a/1/i")),
                TraversalEvent.of(POST, dir1().resolve("a/1/i")),
                TraversalEvent.of(POST, dir1().resolve("a/1")),
                TraversalEvent.of(PRE, dir1().resolve("a/2")),
                TraversalEvent.of(POST, dir1().resolve("a/2")),
                TraversalEvent.of(POST, dir1().resolve("a")),
                TraversalEvent.of(PRE, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1())
        );

        Recorder recorder = new Recorder();
        traverse(dir1(), NOFOLLOW, recorder, SORT_BY_NAME);

        checkEquals(expected, recorder.events);
    }

    @Test
    public void traversal_skip() throws Exception {
        createDirs(dir1().resolve("a/1"));
        createFile(dir1().resolve("a/1/i"));
        createDirs(dir1().resolve("a/2"));
        createDirs(dir1().resolve("b"));

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().resolve("a")),
                TraversalEvent.of(PRE, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1())
        );

        Recorder recorder = new Recorder() {
            @Override
            public Result onPreVisit(Path file) throws IOException {
                super.onPreVisit(file);
                if (file.name().toString().equals("a")) {
                    return SKIP;
                }
                return CONTINUE;
            }
        };
        traverse(dir1(), NOFOLLOW, recorder, SORT_BY_NAME);
        checkEquals(expected, recorder.events);
    }

    @Test
    public void traverse_termination() throws Exception {
        createDirs(dir1().resolve("a/1"));
        createFile(dir1().resolve("a/1/i"));
        createDirs(dir1().resolve("a/2"));
        createDirs(dir1().resolve("b"));

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().resolve("a"))
        );

        Recorder recorder = new Recorder() {
            @Override
            public Result onPreVisit(Path file) throws IOException {
                super.onPreVisit(file);
                if (file.name().toString().equals("a")) {
                    return TERMINATE;
                }
                return CONTINUE;
            }
        };
        traverse(dir1(), NOFOLLOW, recorder, SORT_BY_NAME);
        checkEquals(expected, recorder.events);
    }

    private static void checkEquals(
            List<TraversalEvent> expected,
            List<TraversalEvent> actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("" +
                    "\nexpected:\n" + TextUtils.join("\n", expected) +
                    "\nactual:  \n" + TextUtils.join("\n", actual));
        }
    }

    private static class Recorder implements TraversalCallback<Path> {

        final List<TraversalEvent> events = new ArrayList<>();

        @Override
        public Result onPreVisit(Path file) throws IOException {
            events.add(TraversalEvent.of(PRE, file));
            return CONTINUE;
        }

        @Override
        public Result onPostVisit(Path file) throws IOException {
            events.add(TraversalEvent.of(POST, file));
            return CONTINUE;
        }

        @Override
        public void onException(Path file, IOException e) throws IOException {
            throw e;
        }

    }

    enum TraversalOrder {
        PRE, POST
    }

    @AutoValue
    static abstract class TraversalEvent {

        abstract TraversalOrder order();

        abstract Path resource();

        static TraversalEvent of(TraversalOrder order, Path file) {
            return new AutoValue_LocalFileTraverseTest_TraversalEvent(order, file);
        }
    }

}
