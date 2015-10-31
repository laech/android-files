package l.files.fs.local;

import android.text.TextUtils;

import com.google.auto.value.AutoValue;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import l.files.fs.File;
import l.files.fs.LinkOption;
import l.files.fs.Permission;
import l.files.fs.Visitor;

import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.SKIP;
import static l.files.fs.Visitor.Result.TERMINATE;
import static l.files.fs.local.LocalFileTraverseTest.TraversalOrder.POST;
import static l.files.fs.local.LocalFileTraverseTest.TraversalOrder.PRE;

/**
 * @see LocalFile#list(LinkOption)
 */
public final class LocalFileTraverseTest extends FileBaseTest {

    private static final Comparator<File> SORT_BY_NAME = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.name().toString().compareTo(rhs.name().toString());
        }
    };

    @Test
    public void traverse_noFollowLink() throws Exception {
        File dir = dir1().resolve("dir").createDir();
        File link = dir1().resolve("link").createLink(dir);
        link.resolve("a").createFile();
        link.resolve("b").createFile();

        Recorder recorder = new Recorder();
        link.traverse(NOFOLLOW, recorder);
        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, link),
                TraversalEvent.of(POST, link)
        );
        checkEquals(expected, recorder.events);
    }

    @Test
    public void traverse_followLink_rootOnly() throws Exception {
        dir1().resolve("dir").createDir();
        dir1().resolve("dir/a").createFile();
        dir1().resolve("dir/b").createDir();
        dir1().resolve("dir/b/1").createFile();
        dir1().resolve("dir/c").createLink(dir1().resolve("dir/b"));
        dir1().resolve("link").createLink(dir1().resolve("dir"));

        Recorder recorder = new Recorder();
        dir1().resolve("link").traverse(FOLLOW, recorder, SORT_BY_NAME);
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
        File dir = dir1().resolve("dir").createDir();
        File link = dir1().resolve("link").createLink(dir);
        File a = link.resolve("a").createFile();

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

    @Test
    public void traverse_continuesIfExceptionHandlerDoesNotThrow_pre() throws Exception {
        dir1().resolve("a").createDir();
        dir1().resolve("b").createDir();

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1())
        );

        Recorder recorder = new Recorder() {

            @Override
            public Result onPreVisit(File file) throws IOException {
                if (file.name().toString().equals("a")) {
                    throw new IOException("Test");
                }
                return super.onPreVisit(file);
            }

            @Override
            public void onException(File file, IOException e) throws IOException {
                // Ignore
            }

        };
        dir1().traverse(NOFOLLOW, recorder);

        checkEquals(expected, recorder.events);
    }

    @Test
    public void traverse_continuesIfExceptionHandlerDoesNotThrow_post() throws Exception {
        dir1().resolve("a/1").createDirs();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("b").createDirs();

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
            public Result onPostVisit(File file) throws IOException {
                super.onPostVisit(file);
                if (file.name().toString().equals("1")) {
                    throw new IOException("Test");
                }
                return CONTINUE;
            }

            @Override
            public void onException(File file, IOException e) throws IOException {
                // Ignore
            }

        };
        dir1().traverse(NOFOLLOW, recorder, SORT_BY_NAME);

        checkEquals(expected, recorder.events);
    }

    @Test
    public void traverse_continuesIfExceptionHandlerDoesNotThrow_noPermission() throws Exception {
        dir1().resolve("a/1").createDirs();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirs();
        dir1().resolve("b").createDirs();
        dir1().resolve("a").setPermissions(Collections.<Permission>emptySet());

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
            public void onException(File file, IOException e) {
                // Ignore
            }
        };
        dir1().traverse(NOFOLLOW, recorder, SORT_BY_NAME);

        checkEquals(expected, recorder.events);
    }

    @Test
    public void traverse_order() throws Exception {
        dir1().resolve("a/1").createDirs();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirs();
        dir1().resolve("b").createDirs();

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
        dir1().traverse(NOFOLLOW, recorder, SORT_BY_NAME);

        checkEquals(expected, recorder.events);
    }

    @Test
    public void traversal_skip() throws Exception {
        dir1().resolve("a/1").createDirs();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirs();
        dir1().resolve("b").createDirs();

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().resolve("a")),
                TraversalEvent.of(PRE, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1())
        );

        Recorder recorder = new Recorder() {
            @Override
            public Result onPreVisit(File file) throws IOException {
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

    @Test
    public void traverse_termination() throws Exception {
        dir1().resolve("a/1").createDirs();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirs();
        dir1().resolve("b").createDirs();

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().resolve("a"))
        );

        Recorder recorder = new Recorder() {
            @Override
            public Result onPreVisit(File file) throws IOException {
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

    private static class Recorder implements Visitor {

        final List<TraversalEvent> events = new ArrayList<>();

        @Override
        public Result onPreVisit(File file) throws IOException {
            events.add(TraversalEvent.of(PRE, file));
            return CONTINUE;
        }

        @Override
        public Result onPostVisit(File file) throws IOException {
            events.add(TraversalEvent.of(POST, file));
            return CONTINUE;
        }

        @Override
        public void onException(File file, IOException e) throws IOException {
            throw e;
        }

    }

    enum TraversalOrder {
        PRE, POST
    }

    @AutoValue
    static abstract class TraversalEvent {

        abstract TraversalOrder order();

        abstract File resource();

        static TraversalEvent of(TraversalOrder order, File file) {
            return new AutoValue_LocalFileTraverseTest_TraversalEvent(order, file);
        }
    }

}
