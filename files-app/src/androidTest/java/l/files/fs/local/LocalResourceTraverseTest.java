package l.files.fs.local;

import com.google.common.base.Joiner;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.ResourceExceptionHandler;
import l.files.fs.ResourceVisitor;
import l.files.fs.ResourceVisitor.Result;

import static java.util.Arrays.asList;
import static l.files.fs.ResourceVisitor.Result.CONTINUE;
import static l.files.fs.ResourceVisitor.Result.SKIP;
import static l.files.fs.ResourceVisitor.Result.TERMINATE;
import static l.files.fs.local.LocalResourceTraverseTest.Order.POST;
import static l.files.fs.local.LocalResourceTraverseTest.Order.PRE;

public final class LocalResourceTraverseTest extends ResourceBaseTest {

    public void testTraversalContinuesIfExceptionHandlerDoesNotThrow_pre() throws Exception {
        dir1().resolve("a").createDirectory();
        dir1().resolve("b").createDirectory();

        List<Entry<Order, Resource>> expected = asList(
                event(PRE, dir1()),
                event(PRE, dir1().resolve("b")),
                event(POST, dir1().resolve("b")),
                event(POST, dir1())
        );

        Recorder recorder = new Recorder() {
            @Override
            Result acceptPre(Resource resource) throws IOException {
                if (resource.getName().equals("a")) {
                    throw new IOException("Test");
                }
                return super.acceptPre(resource);
            }
        };
        dir1().traverse(
                recorder.getPreVisitor(),
                recorder.getPostVisitor(),
                ignoreException()
        );

        checkEquals(expected, recorder.getEvents());
    }

    public void testTraversalContinuesIfExceptionHandlerDoesNotThrow_post() throws Exception {
        dir1().resolve("a/1").createDirectories();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("b").createDirectories();

        List<Entry<Order, Resource>> expected = asList(
                event(PRE, dir1()),
                event(PRE, dir1().resolve("a")),
                event(PRE, dir1().resolve("a/1")),
                event(PRE, dir1().resolve("a/1/i")),
                event(POST, dir1().resolve("a/1/i")),
                event(POST, dir1().resolve("a/1")),
                event(POST, dir1().resolve("a")),
                event(PRE, dir1().resolve("b")),
                event(POST, dir1().resolve("b")),
                event(POST, dir1())
        );

        Recorder recorder = new Recorder() {
            @Override
            Result acceptPost(Resource resource) throws IOException {
                super.acceptPost(resource);
                if (resource.getName().equals("1")) {
                    throw new IOException("Test");
                }
                return CONTINUE;
            }
        };
        dir1().traverse(
                recorder.getPreVisitor(),
                recorder.getPostVisitor(),
                ignoreException()
        );

        checkEquals(expected, recorder.getEvents());
    }

    public void testTraversalContinuesIfExceptionHandlerDoesNotThrow_noPermission() throws Exception {
        dir1().resolve("a/1").createDirectories();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirectories();
        dir1().resolve("b").createDirectories();
        dir1().resolve("a").setPermissions(Collections.<Permission>emptySet());

        List<Entry<Order, Resource>> expected = asList(
                event(PRE, dir1()),
                event(PRE, dir1().resolve("a")),
                event(POST, dir1().resolve("a")),
                event(PRE, dir1().resolve("b")),
                event(POST, dir1().resolve("b")),
                event(POST, dir1())
        );

        Recorder recorder = new Recorder();
        dir1().traverse(
                recorder.getPreVisitor(),
                recorder.getPostVisitor(),
                ignoreException()
        );

        checkEquals(expected, recorder.getEvents());
    }

    private static ResourceExceptionHandler ignoreException() {
        return new ResourceExceptionHandler() {
            @Override
            public void handle(Resource resource, IOException e)
                    throws IOException {
                // no throw
            }
        };
    }

    public void testTraversalOrder() throws Exception {
        dir1().resolve("a/1").createDirectories();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirectories();
        dir1().resolve("b").createDirectories();

        List<Entry<Order, Resource>> expected = asList(
                event(PRE, dir1()),
                event(PRE, dir1().resolve("a")),
                event(PRE, dir1().resolve("a/1")),
                event(PRE, dir1().resolve("a/1/i")),
                event(POST, dir1().resolve("a/1/i")),
                event(POST, dir1().resolve("a/1")),
                event(PRE, dir1().resolve("a/2")),
                event(POST, dir1().resolve("a/2")),
                event(POST, dir1().resolve("a")),
                event(PRE, dir1().resolve("b")),
                event(POST, dir1().resolve("b")),
                event(POST, dir1())
        );

        Recorder recorder = new Recorder();
        dir1().traverse(recorder.getPreVisitor(), recorder.getPostVisitor());

        checkEquals(expected, recorder.getEvents());
    }

    public void testTraversalSkipping() throws Exception {
        dir1().resolve("a/1").createDirectories();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirectories();
        dir1().resolve("b").createDirectories();

        List<Entry<Order, Resource>> expected = asList(
                event(PRE, dir1()),
                event(PRE, dir1().resolve("a")),
                event(PRE, dir1().resolve("b")),
                event(POST, dir1().resolve("b")),
                event(POST, dir1())
        );

        Recorder recorder = new Recorder() {
            @Override
            Result acceptPre(Resource resource) throws IOException {
                super.acceptPre(resource);
                if (resource.getName().equals("a")) {
                    return SKIP;
                }
                return CONTINUE;
            }
        };
        dir1().traverse(recorder.getPreVisitor(), recorder.getPostVisitor());

        checkEquals(expected, recorder.getEvents());

    }

    public void testTraversalTermination() throws Exception {
        dir1().resolve("a/1").createDirectories();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirectories();
        dir1().resolve("b").createDirectories();

        List<Entry<Order, Resource>> expected = asList(
                event(PRE, dir1()),
                event(PRE, dir1().resolve("a"))
        );

        Recorder recorder = new Recorder() {
            @Override
            Result acceptPre(Resource resource) throws IOException {
                super.acceptPre(resource);
                if (resource.getName().equals("a")) {
                    return TERMINATE;
                }
                return CONTINUE;
            }
        };
        dir1().traverse(recorder.getPreVisitor(), recorder.getPostVisitor());

        checkEquals(expected, recorder.getEvents());

    }

    private void checkEquals(
            List<? extends Entry<Order, ? extends Resource>> expected,
            List<? extends Entry<Order, ? extends Resource>> actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("" +
                    "\nexpected:\n" + Joiner.on('\n').join(expected) +
                    "\nactual:  \n" + Joiner.on('\n').join(actual));
        }
    }

    private static class Recorder {

        private final List<Entry<Order, Resource>> events = new ArrayList<>();

        List<Entry<Order, Resource>> getEvents() {
            return events;
        }

        private final ResourceVisitor pre = new ResourceVisitor() {
            @Override
            public Result accept(Resource resource) throws IOException {
                return acceptPre(resource);
            }
        };

        Result acceptPre(Resource resource) throws IOException {
            events.add(event(PRE, resource));
            return CONTINUE;
        }

        ResourceVisitor getPreVisitor() {
            return pre;
        }

        private final ResourceVisitor post = new ResourceVisitor() {
            @Override
            public Result accept(Resource resource) throws IOException {
                return acceptPost(resource);
            }
        };

        Result acceptPost(Resource resource) throws IOException {
            events.add(event(POST, resource));
            return CONTINUE;
        }

        ResourceVisitor getPostVisitor() {
            return post;
        }
    }

    private static Entry<Order, Resource> event(Order order, Resource resource) {
        return new SimpleEntry<>(order, resource);
    }

    enum Order {
        PRE, POST
    }

}
