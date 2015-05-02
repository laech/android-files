package l.files.fs.local;

import com.google.common.base.Joiner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import auto.parcel.AutoParcel;
import l.files.fs.LinkOption;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.ExceptionHandler;
import l.files.fs.Visitor;

import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.SKIP;
import static l.files.fs.Visitor.Result.TERMINATE;
import static l.files.fs.local.LocalResource_list_Test.TraversalOrder.POST;
import static l.files.fs.local.LocalResource_list_Test.TraversalOrder.PRE;

/**
 * @see LocalResource#list(LinkOption, Visitor)
 * @see LocalResource#list(LinkOption, Collection)
 * @see LocalResource#list(LinkOption)
 */
public final class LocalResource_list_Test extends ResourceBaseTest {

    public void test_traverse_noFollowLink() throws Exception {
        Resource dir = dir1().resolve("dir").createDirectory();
        Resource link = dir1().resolve("link").createLink(dir);
        link.resolve("a").createFile();
        link.resolve("b").createFile();

        Recorder recorder = new Recorder();
        link.traverse(NOFOLLOW, recorder.getPreVisitor(), recorder.getPostVisitor());
        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, link),
                TraversalEvent.of(POST, link)
        );
        checkEquals(expected, recorder.getEvents());
    }

    public void test_traverse_followLink_rootOnly() throws Exception {
        dir1().resolve("dir").createDirectory();
        dir1().resolve("dir/a").createFile();
        dir1().resolve("dir/b").createDirectory();
        dir1().resolve("dir/b/1").createFile();
        dir1().resolve("dir/c").createLink(dir1().resolve("dir/b"));
        dir1().resolve("link").createLink(dir1().resolve("dir"));

        Recorder recorder = new Recorder();
        dir1().resolve("link").traverse(
                FOLLOW,
                recorder.getPreVisitor(),
                recorder.getPostVisitor()
        );
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
        checkEquals(expected, recorder.getEvents());
    }

    public void test_traverse_followLink() throws Exception {
        Resource dir = dir1().resolve("dir").createDirectory();
        Resource link = dir1().resolve("link").createLink(dir);
        Resource a = link.resolve("a").createFile();

        Recorder recorder = new Recorder();
        link.traverse(FOLLOW, recorder.getPreVisitor(), recorder.getPostVisitor());
        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, link),
                TraversalEvent.of(PRE, a),
                TraversalEvent.of(POST, a),
                TraversalEvent.of(POST, link)
        );
        checkEquals(expected, recorder.getEvents());
    }

    public void test_traverse_continuesIfExceptionHandlerDoesNotThrow_pre() throws Exception {
        dir1().resolve("a").createDirectory();
        dir1().resolve("b").createDirectory();

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1())
        );

        Recorder recorder = new Recorder() {
            @Override
            Visitor.Result acceptPre(Resource resource) throws IOException {
                if (resource.name().equals("a")) {
                    throw new IOException("Test");
                }
                return super.acceptPre(resource);
            }
        };
        dir1().traverse(
                NOFOLLOW,
                recorder.getPreVisitor(),
                recorder.getPostVisitor(),
                ignoreException()
        );

        checkEquals(expected, recorder.getEvents());
    }

    public void test_traverse_continuesIfExceptionHandlerDoesNotThrow_post() throws Exception {
        dir1().resolve("a/1").createDirectories();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("b").createDirectories();

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
            Visitor.Result acceptPost(Resource resource) throws IOException {
                super.acceptPost(resource);
                if (resource.name().equals("1")) {
                    throw new IOException("Test");
                }
                return CONTINUE;
            }
        };
        dir1().traverse(
                NOFOLLOW,
                recorder.getPreVisitor(),
                recorder.getPostVisitor(),
                ignoreException()
        );

        checkEquals(expected, recorder.getEvents());
    }

    public void test_traverse_continuesIfExceptionHandlerDoesNotThrow_noPermission() throws Exception {
        dir1().resolve("a/1").createDirectories();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirectories();
        dir1().resolve("b").createDirectories();
        dir1().resolve("a").setPermissions(Collections.<Permission>emptySet());

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().resolve("a")),
                TraversalEvent.of(POST, dir1().resolve("a")),
                TraversalEvent.of(PRE, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1())
        );

        Recorder recorder = new Recorder();
        dir1().traverse(
                NOFOLLOW,
                recorder.getPreVisitor(),
                recorder.getPostVisitor(),
                ignoreException()
        );

        checkEquals(expected, recorder.getEvents());
    }

    private static ExceptionHandler ignoreException() {
        return new ExceptionHandler() {
            @Override
            public void handle(Resource resource, IOException e)
                    throws IOException {
                // no throw
            }
        };
    }

    public void test_traverse_order() throws Exception {
        dir1().resolve("a/1").createDirectories();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirectories();
        dir1().resolve("b").createDirectories();

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
        dir1().traverse(
                NOFOLLOW,
                recorder.getPreVisitor(),
                recorder.getPostVisitor()
        );

        checkEquals(expected, recorder.getEvents());
    }

    public void test_traversal_skip() throws Exception {
        dir1().resolve("a/1").createDirectories();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirectories();
        dir1().resolve("b").createDirectories();

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().resolve("a")),
                TraversalEvent.of(PRE, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1().resolve("b")),
                TraversalEvent.of(POST, dir1())
        );

        Recorder recorder = new Recorder() {
            @Override
            Visitor.Result acceptPre(Resource resource) throws IOException {
                super.acceptPre(resource);
                if (resource.name().equals("a")) {
                    return SKIP;
                }
                return CONTINUE;
            }
        };
        dir1().traverse(
                NOFOLLOW,
                recorder.getPreVisitor(),
                recorder.getPostVisitor()
        );

        checkEquals(expected, recorder.getEvents());

    }

    public void test_traverse_termination() throws Exception {
        dir1().resolve("a/1").createDirectories();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirectories();
        dir1().resolve("b").createDirectories();

        List<TraversalEvent> expected = asList(
                TraversalEvent.of(PRE, dir1()),
                TraversalEvent.of(PRE, dir1().resolve("a"))
        );

        Recorder recorder = new Recorder() {
            @Override
            Visitor.Result acceptPre(Resource resource) throws IOException {
                super.acceptPre(resource);
                if (resource.name().equals("a")) {
                    return TERMINATE;
                }
                return CONTINUE;
            }
        };
        dir1().traverse(
                NOFOLLOW,
                recorder.getPreVisitor(),
                recorder.getPostVisitor()
        );

        checkEquals(expected, recorder.getEvents());

    }

    private static void checkEquals(
            List<TraversalEvent> expected,
            List<TraversalEvent> actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("" +
                    "\nexpected:\n" + Joiner.on('\n').join(expected) +
                    "\nactual:  \n" + Joiner.on('\n').join(actual));
        }
    }

    private static class Recorder {

        private final List<TraversalEvent> events = new ArrayList<>();

        List<TraversalEvent> getEvents() {
            return events;
        }

        private final Visitor pre = new Visitor() {
            @Override
            public Result accept(Resource resource) throws IOException {
                return acceptPre(resource);
            }
        };

        Visitor.Result acceptPre(Resource resource) throws IOException {
            events.add(TraversalEvent.of(PRE, resource));
            return CONTINUE;
        }

        Visitor getPreVisitor() {
            return pre;
        }

        private final Visitor post = new Visitor() {
            @Override
            public Result accept(Resource resource) throws IOException {
                return acceptPost(resource);
            }
        };

        Visitor.Result acceptPost(Resource resource) throws IOException {
            events.add(TraversalEvent.of(POST, resource));
            return CONTINUE;
        }

        Visitor getPostVisitor() {
            return post;
        }

    }

    enum TraversalOrder {
        PRE, POST
    }

    @AutoParcel
    static abstract class TraversalEvent {

        abstract TraversalOrder order();

        abstract Resource resource();

        static TraversalEvent of(TraversalOrder order, Resource resource) {
            return new AutoParcel_LocalResource_list_Test_TraversalEvent(order, resource);
        }
    }

}
