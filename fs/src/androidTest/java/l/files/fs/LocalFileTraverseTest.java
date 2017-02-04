package l.files.fs;

import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.TraversalCallback.Result.CONTINUE;
import static l.files.fs.TraversalCallback.Result.SKIP;
import static l.files.fs.TraversalCallback.Result.TERMINATE;
import static l.files.fs.LocalFileTraverseTest.TraversalOrder.POST;
import static l.files.fs.LocalFileTraverseTest.TraversalOrder.PRE;

public final class LocalFileTraverseTest extends PathBaseTest {

    private static final Comparator<Path> SORT_BY_NAME = new Comparator<Path>() {
        @Override
        public int compare(Path a, Path b) {
            return a.name().toString().compareTo(b.name().toString());
        }
    };

    public void test_traverse_noFollowLink() throws Exception {
        Path dir = dir1().concat("dir").createDirectory();
        Path link = dir1().concat("link").createSymbolicLink(dir);
        link.concat("a").createFile();
        link.concat("b").createFile();

        Recorder recorder = new Recorder();
        link.traverse(NOFOLLOW, recorder);
        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, link),
                TraversalEvent.of(POST, link)
        );
        checkEquals(expected, recorder.events);
    }

    public void test_traverse_followLink_rootOnly() throws Exception {
        dir1().concat("dir").createDirectory();
        dir1().concat("dir/a").createFile();
        dir1().concat("dir/b").createDirectory();
        dir1().concat("dir/b/1").createFile();
        dir1().concat("dir/c").createSymbolicLink(dir1().concat("dir/b"));
        dir1().concat("link").createSymbolicLink(dir1().concat("dir"));

        Recorder recorder = new Recorder();
        dir1().concat("link").traverse(FOLLOW, recorder, SORT_BY_NAME);
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
        Path dir = dir1().concat("dir").createDirectory();
        Path link = dir1().concat("link").createSymbolicLink(dir);
        Path a = link.concat("a").createFile();

        Recorder recorder = new Recorder();
        link.traverse(FOLLOW, recorder);
        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, link),
                TraversalEvent.of(PRE, a),
                TraversalEvent.of(POST, a),
                TraversalEvent.of(POST, link)
        );
        checkEquals(expected, recorder.events);
    }

    public void test_traverse_continuesIfExceptionHandlerDoesNotThrow_pre() throws Exception {
        dir1().concat("a").createDirectory();
        dir1().concat("b").createDirectory();

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
        dir1().traverse(NOFOLLOW, recorder);

        checkEquals(expected, recorder.events);
    }

    public void test_traverse_continuesIfExceptionHandlerDoesNotThrow_post() throws Exception {
        dir1().concat("a/1").createDirectories();
        dir1().concat("a/1/i").createFile();
        dir1().concat("b").createDirectories();

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
        dir1().traverse(NOFOLLOW, recorder, SORT_BY_NAME);

        checkEquals(expected, recorder.events);
    }

    public void test_traverse_continuesIfExceptionHandlerDoesNotThrow_noPermission() throws Exception {
        dir1().concat("a/1").createDirectories();
        dir1().concat("a/1/i").createFile();
        dir1().concat("a/2").createDirectories();
        dir1().concat("b").createDirectories();
        dir1().concat("a").setPermissions(Permission.none());

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
        dir1().traverse(NOFOLLOW, recorder, SORT_BY_NAME);

        checkEquals(expected, recorder.events);
    }

    public void test_traverse_order() throws Exception {
        dir1().concat("a/1").createDirectories();
        dir1().concat("a/1/i").createFile();
        dir1().concat("a/2").createDirectories();
        dir1().concat("b").createDirectories();

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
        dir1().traverse(NOFOLLOW, recorder, SORT_BY_NAME);

        checkEquals(expected, recorder.events);
    }

    public void test_traversal_skip() throws Exception {
        dir1().concat("a/1").createDirectories();
        dir1().concat("a/1/i").createFile();
        dir1().concat("a/2").createDirectories();
        dir1().concat("b").createDirectories();

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
        dir1().traverse(NOFOLLOW, recorder, SORT_BY_NAME);
        checkEquals(expected, recorder.events);
    }

    public void test_traverse_termination() throws Exception {
        dir1().concat("a/1").createDirectories();
        dir1().concat("a/1/i").createFile();
        dir1().concat("a/2").createDirectories();
        dir1().concat("b").createDirectories();

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
        dir1().traverse(NOFOLLOW, recorder, SORT_BY_NAME);
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
