package l.files.fs.local;

import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.TraversalCallback;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static l.files.base.Objects.requireNonNull;
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

    public LocalFileTraverseTest() {
        super(LocalFileSystem.INSTANCE);
    }

    public void test_traverse_noFollowLink() throws Exception {
        Path dir = fs.createDir(dir1().concat("dir"));
        Path link = fs.createSymbolicLink(dir1().concat("link"), dir);
        fs.createFile(link.concat("a"));
        fs.createFile(link.concat("b"));

        Recorder recorder = new Recorder();
        fs.traverse(link, NOFOLLOW, recorder);
        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, link),
                TraversalEvent.of(POST, link)
        );
        checkEquals(expected, recorder.events);
    }

    public void test_traverse_followLink_rootOnly() throws Exception {
        fs.createDir(dir1().concat("dir"));
        fs.createFile(dir1().concat("dir/a"));
        fs.createDir(dir1().concat("dir/b"));
        fs.createFile(dir1().concat("dir/b/1"));
        fs.createSymbolicLink(dir1().concat("dir/c"), dir1().concat("dir/b"));
        fs.createSymbolicLink(dir1().concat("link"), dir1().concat("dir"));

        Recorder recorder = new Recorder();
        fs.traverse(dir1().concat("link"), FOLLOW, recorder, SORT_BY_NAME);
        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1().concat("link")),
                TraversalEvent.of(PRE, dir1().concat("link/a")),
                TraversalEvent.of(POST, dir1().concat("link/a")),
                TraversalEvent.of(PRE, dir1().concat("link/b")),
                TraversalEvent.of(PRE, dir1().concat("link/b/1")),
                TraversalEvent.of(POST, dir1().concat("link/b/1")),
                TraversalEvent.of(POST, dir1().concat("link/b")),
                TraversalEvent.of(PRE, dir1().concat("link/c")),
                // link/c is not followed into
                TraversalEvent.of(POST, dir1().concat("link/c")),
                TraversalEvent.of(POST, dir1().concat("link"))
        );
        checkEquals(expected, recorder.events);
    }

    public void test_traverse_followLink() throws Exception {
        Path dir = fs.createDir(dir1().concat("dir"));
        Path link = fs.createSymbolicLink(dir1().concat("link"), dir);
        Path a = fs.createFile(link.concat("a"));

        Recorder recorder = new Recorder();
        fs.traverse(link, FOLLOW, recorder);
        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, link),
                TraversalEvent.of(PRE, a),
                TraversalEvent.of(POST, a),
                TraversalEvent.of(POST, link)
        );
        checkEquals(expected, recorder.events);
    }

    public void test_traverse_continuesIfExceptionHandlerDoesNotThrow_pre() throws Exception {
        fs.createDir(dir1().concat("a"));
        fs.createDir(dir1().concat("b"));

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().concat("b")),
                TraversalEvent.of(POST, dir1().concat("b")),
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
        fs.traverse(dir1(), NOFOLLOW, recorder);

        checkEquals(expected, recorder.events);
    }

    public void test_traverse_continuesIfExceptionHandlerDoesNotThrow_post() throws Exception {
        fs.createDirs(dir1().concat("a/1"));
        fs.createFile(dir1().concat("a/1/i"));
        fs.createDirs(dir1().concat("b"));

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().concat("a")),
                TraversalEvent.of(PRE, dir1().concat("a/1")),
                TraversalEvent.of(PRE, dir1().concat("a/1/i")),
                TraversalEvent.of(POST, dir1().concat("a/1/i")),
                TraversalEvent.of(POST, dir1().concat("a/1")),
                TraversalEvent.of(POST, dir1().concat("a")),
                TraversalEvent.of(PRE, dir1().concat("b")),
                TraversalEvent.of(POST, dir1().concat("b")),
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
        fs.traverse(dir1(), NOFOLLOW, recorder, SORT_BY_NAME);

        checkEquals(expected, recorder.events);
    }

    public void test_traverse_continuesIfExceptionHandlerDoesNotThrow_noPermission() throws Exception {
        fs.createDirs(dir1().concat("a/1"));
        fs.createFile(dir1().concat("a/1/i"));
        fs.createDirs(dir1().concat("a/2"));
        fs.createDirs(dir1().concat("b"));
        fs.setPermissions(dir1().concat("a"), Permission.none());

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().concat("a")),
                TraversalEvent.of(POST, dir1().concat("a")),
                TraversalEvent.of(PRE, dir1().concat("b")),
                TraversalEvent.of(POST, dir1().concat("b")),
                TraversalEvent.of(POST, dir1())
        );

        Recorder recorder = new Recorder() {
            @Override
            public void onException(Path file, IOException e) {
                // Ignore
            }
        };
        fs.traverse(dir1(), NOFOLLOW, recorder, SORT_BY_NAME);

        checkEquals(expected, recorder.events);
    }

    public void test_traverse_order() throws Exception {
        fs.createDirs(dir1().concat("a/1"));
        fs.createFile(dir1().concat("a/1/i"));
        fs.createDirs(dir1().concat("a/2"));
        fs.createDirs(dir1().concat("b"));

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().concat("a")),
                TraversalEvent.of(PRE, dir1().concat("a/1")),
                TraversalEvent.of(PRE, dir1().concat("a/1/i")),
                TraversalEvent.of(POST, dir1().concat("a/1/i")),
                TraversalEvent.of(POST, dir1().concat("a/1")),
                TraversalEvent.of(PRE, dir1().concat("a/2")),
                TraversalEvent.of(POST, dir1().concat("a/2")),
                TraversalEvent.of(POST, dir1().concat("a")),
                TraversalEvent.of(PRE, dir1().concat("b")),
                TraversalEvent.of(POST, dir1().concat("b")),
                TraversalEvent.of(POST, dir1())
        );

        Recorder recorder = new Recorder();
        fs.traverse(dir1(), NOFOLLOW, recorder, SORT_BY_NAME);

        checkEquals(expected, recorder.events);
    }

    public void test_traversal_skip() throws Exception {
        fs.createDirs(dir1().concat("a/1"));
        fs.createFile(dir1().concat("a/1/i"));
        fs.createDirs(dir1().concat("a/2"));
        fs.createDirs(dir1().concat("b"));

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().concat("a")),
                TraversalEvent.of(PRE, dir1().concat("b")),
                TraversalEvent.of(POST, dir1().concat("b")),
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
        fs.traverse(dir1(), NOFOLLOW, recorder, SORT_BY_NAME);
        checkEquals(expected, recorder.events);
    }

    public void test_traverse_termination() throws Exception {
        fs.createDirs(dir1().concat("a/1"));
        fs.createFile(dir1().concat("a/1/i"));
        fs.createDirs(dir1().concat("a/2"));
        fs.createDirs(dir1().concat("b"));

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().concat("a"))
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
        fs.traverse(dir1(), NOFOLLOW, recorder, SORT_BY_NAME);
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

    static final class TraversalEvent {

        final TraversalOrder order;
        final Path path;

        private TraversalEvent(TraversalOrder order, Path path) {
            this.order = requireNonNull(order);
            this.path = requireNonNull(path);
        }

        static TraversalEvent of(TraversalOrder order, Path file) {
            return new TraversalEvent(order, file);
        }

        @Override
        public String toString() {
            return "TraversalEvent{" +
                    "order=" + order +
                    ", path=" + path +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TraversalEvent that = (TraversalEvent) o;

            return order == that.order &&
                    path.equals(that.path);

        }

        @Override
        public int hashCode() {
            int result = order.hashCode();
            result = 31 * result + path.hashCode();
            return result;
        }
    }

}
