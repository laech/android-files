package l.files.fs.local;

import com.google.common.base.Joiner;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import l.files.fs.Resource;
import l.files.fs.ResourceVisitor;
import l.files.fs.ResourceVisitor.Order;

import static l.files.fs.ResourceVisitor.Order.POST;
import static l.files.fs.ResourceVisitor.Order.PRE;
import static l.files.fs.ResourceVisitor.Result.CONTINUE;
import static l.files.fs.ResourceVisitor.Result.SKIP;
import static l.files.fs.ResourceVisitor.Result.TERMINATE;

public final class LocalResourceTraverseTest extends ResourceBaseTest {

    public void testTraversalOrder() throws Exception {
        dir1().resolve("a/1").createDirectories();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirectories();
        dir1().resolve("b").createDirectories();

        List<SimpleEntry<Order, LocalResource>> expected = Arrays.asList(
                new SimpleEntry<>(PRE, dir1()),
                new SimpleEntry<>(PRE, dir1().resolve("a")),
                new SimpleEntry<>(PRE, dir1().resolve("a/1")),
                new SimpleEntry<>(PRE, dir1().resolve("a/1/i")),
                new SimpleEntry<>(POST, dir1().resolve("a/1/i")),
                new SimpleEntry<>(POST, dir1().resolve("a/1")),
                new SimpleEntry<>(PRE, dir1().resolve("a/2")),
                new SimpleEntry<>(POST, dir1().resolve("a/2")),
                new SimpleEntry<>(POST, dir1().resolve("a")),
                new SimpleEntry<>(PRE, dir1().resolve("b")),
                new SimpleEntry<>(POST, dir1().resolve("b")),
                new SimpleEntry<>(POST, dir1())
        );

        Recorder recorder = new Recorder();
        dir1().traverse(recorder);

        checkEquals(expected, recorder.getEvents());
    }

    public void testTraversalSkipping() throws Exception {
        dir1().resolve("a/1").createDirectories();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirectories();
        dir1().resolve("b").createDirectories();

        List<SimpleEntry<Order, LocalResource>> expected = Arrays.asList(
                new SimpleEntry<>(PRE, dir1()),
                new SimpleEntry<>(PRE, dir1().resolve("a")),
                new SimpleEntry<>(PRE, dir1().resolve("b")),
                new SimpleEntry<>(POST, dir1().resolve("b")),
                new SimpleEntry<>(POST, dir1())
        );

        Recorder recorder = new Recorder() {
            @Override
            public Result accept(Order order, Resource resource) throws IOException {
                super.accept(order, resource);
                if (order == PRE && resource.getName().equals("a")) {
                    return SKIP;
                }
                return CONTINUE;
            }
        };
        dir1().traverse(recorder);

        checkEquals(expected, recorder.getEvents());

    }

    public void testTraversalTermination() throws Exception {
        dir1().resolve("a/1").createDirectories();
        dir1().resolve("a/1/i").createFile();
        dir1().resolve("a/2").createDirectories();
        dir1().resolve("b").createDirectories();

        List<SimpleEntry<Order, LocalResource>> expected = Arrays.asList(
                new SimpleEntry<>(PRE, dir1()),
                new SimpleEntry<>(PRE, dir1().resolve("a"))
        );

        Recorder recorder = new Recorder() {
            @Override
            public Result accept(Order order, Resource resource) throws IOException {
                super.accept(order, resource);
                if (order == PRE && resource.getName().equals("a")) {
                    return TERMINATE;
                }
                return CONTINUE;
            }
        };
        dir1().traverse(recorder);

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

    private static class Recorder implements ResourceVisitor {

        private final List<Entry<Order, Resource>> events = new ArrayList<>();

        List<Entry<Order, Resource>> getEvents() {
            return events;
        }

        @Override
        public Result accept(Order order, Resource resource) throws IOException {
            events.add(new SimpleEntry<>(order, resource));
            return CONTINUE;
        }
    }

}
