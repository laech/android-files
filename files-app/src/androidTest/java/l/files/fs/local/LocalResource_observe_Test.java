package l.files.fs.local;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nullable;

import auto.parcel.AutoParcel;
import l.files.fs.Event;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Observer;
import l.files.fs.Permission;
import l.files.fs.Resource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.Event.CREATE;
import static l.files.fs.Event.DELETE;
import static l.files.fs.Event.MODIFY;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.OWNER_WRITE;
import static l.files.fs.local.LocalResource_observe_Test.Recorder.observe;

/**
 * @see Resource#observe(LinkOption, Observer)
 */
public final class LocalResource_observe_Test extends ResourceBaseTest
{
    public void testObserveOnLinkNoFollow() throws Exception
    {
        final Resource dir = dir1().resolve("dir").createDirectory();
        final Resource link = dir1().resolve("link").createLink(dir);
        final Resource file = link.resolve("file");
        try (Recorder observer = observe(link, NOFOLLOW))
        {
            // No follow, can observe the link
            observer.await(MODIFY, link,
                    newSetModificationTime(link, NOFOLLOW, Instant.of(1, 1)));
            // But not the child content
            try
            {
                observer.await(CREATE, file, newCreateFile(file));
            }
            catch (final AssertionError e)
            {
                assertTrue(observer.actual.isEmpty());
                return;
            }
            fail();
        }
    }

    public void testObserveOnLinkFollow() throws Exception
    {
        final Resource dir = dir1().resolve("dir").createDirectory();
        final Resource link = dir1().resolve("link").createLink(dir);
        final Resource child = link.resolve("dir").createDirectory();
        try (Recorder observer = observe(link, FOLLOW))
        {
            observer.await(MODIFY, child, newCreateFile(child.resolve("a")));
        }
    }

    public void testMoveDirectoryInThenAddFileIntoIt() throws Exception
    {
        final Resource dst = dir1().resolve("a");
        final Resource src = dir2().resolve("a").createDirectory();
        try (Recorder observer = observe(dir1()))
        {
            observer.await(CREATE, dst, newMove(src, dst));
            observer.await(MODIFY, dst, newCreateDirectory(dst.resolve("b")));
        }
    }

    public void testMoveDirectoryInThenDeleteFileFromIt() throws Exception
    {
        final Resource dstDir = dir1().resolve("a");
        final Resource srcDir = dir2().resolve("a").createDirectory();
        srcDir.resolve("b").createFile();
        try (Recorder observer = observe(dir1()))
        {
            observer.await(CREATE, dstDir, newMove(srcDir, dstDir));
            observer.await(MODIFY, dstDir, newDelete(dstDir.resolve("b")));
        }
    }

    public void testMoveDirectoryInThenMoveFileIntoIt() throws Exception
    {
        final Resource dir = dir1().resolve("a");
        final Resource src1 = dir2().resolve("a").createDirectory();
        final Resource src2 = dir2().resolve("b").createFile();
        try (Recorder observer = observe(dir1()))
        {
            observer.await(CREATE, dir, newMove(src1, dir));
            observer.await(MODIFY, dir, newMove(src2, dir.resolve("b")));
        }
    }

    public void testMoveDirectoryInThenMoveFileOutOfIt() throws Exception
    {
        final Resource src = dir2().resolve("a").createDirectory();
        final Resource dir = dir1().resolve("a");
        final Resource child = dir.resolve("b");
        try (Recorder observer = observe(dir1()))
        {
            observer.await(CREATE, dir, newMove(src, dir));
            observer.await(
                    asList(
                            event(MODIFY, dir),
                            event(MODIFY, dir)
                    ),
                    compose(
                            newCreateFile(child),
                            newMove(child, dir2().resolve("b"))
                    )
            );
        }
    }

    public void testMoveFileIn() throws Exception
    {
        final Resource src = dir2().resolve("a").createFile();
        final Resource dst = dir1().resolve("b");
        try (Recorder observer = observe(dir1()))
        {
            observer.await(CREATE, dst, newMove(src, dst));
        }
    }

    public void testMoveFileOut() throws Exception
    {
        final Resource file = dir1().resolve("a").createFile();
        try (Recorder observer = observe(dir1()))
        {
            observer.await(DELETE, file, newMove(file, dir2().resolve("a")));
        }
    }

    public void testMoveSelfOut() throws Exception
    {
        final Resource file = dir1().resolve("file").createFile();
        final Resource dir = dir1().resolve("dir").createDirectory();
        testMoveSelfOut(file, dir2().resolve("a"));
        testMoveSelfOut(dir, dir2().resolve("b"));
    }

    private static void testMoveSelfOut(
            final Resource src,
            final Resource dst) throws Exception
    {
        try (Recorder observer = observe(src))
        {
            observer.await(DELETE, src, newMove(src, dst));
        }
    }

    public void testModifyFileContent() throws Exception
    {
        final Resource file = dir1().resolve("a").createFile();
        testModifyFileContent(file, file);
        testModifyFileContent(file, dir1());
    }

    private static void testModifyFileContent(
            final Resource file,
            final Resource observable) throws Exception
    {
        try (Recorder observer = observe(observable))
        {
            observer.await(MODIFY, file, newAppend(file, "abc"));
        }
    }

    public void testModifyPermissions() throws Exception
    {
        final Resource file = dir1().resolve("file").createFile();
        final Resource dir = dir1().resolve("directory").createDirectory();
        testModifyPermission(file, file);
        testModifyPermission(file, dir1());
        testModifyPermission(dir, dir);
        testModifyPermission(dir, dir1());
    }

    private static void testModifyPermission(
            final Resource target,
            final Resource observable) throws Exception
    {
        final Set<Permission> oldPerms = target.stat(NOFOLLOW).permissions();
        final Set<Permission> newPerms;
        if (oldPerms.isEmpty())
        {
            newPerms = singleton(OWNER_WRITE);
        }
        else
        {
            newPerms = emptySet();
        }
        try (Recorder observer = observe(observable))
        {
            observer.await(MODIFY, target, newSetPermissions(target, newPerms));
        }
    }

    /*
     * Note: IN_MODIFY is fired instead of the expected IN_ATTRIB when changing
     * the last modified time, and when changing access time both are not fired
     * making it not untrackable.
     */
    public void testModifyModificationTime() throws Exception
    {
        final Resource file = dir1().resolve("file").createFile();
        final Resource dir = dir1().resolve("dir").createDirectory();
        testModifyModificationTime(file, file);
        testModifyModificationTime(file, dir1());
        testModifyModificationTime(dir, dir);
        testModifyModificationTime(dir, dir1());
    }

    private void testModifyModificationTime(
            final Resource target,
            final Resource observable) throws Exception
    {
        final Instant old = target.stat(NOFOLLOW).modificationTime();
        final Instant t = Instant.of(old.seconds() - 1, old.nanos());
        try (Recorder observer = observe(observable))
        {
            observer.await(MODIFY, target,
                    newSetModificationTime(target, NOFOLLOW, t));
        }
    }

    public void testDelete() throws Exception
    {
        final Resource file = dir1().resolve("file");
        final Resource dir = dir1().resolve("dir");
        testDelete(file.createFile(), file);
        testDelete(file.createFile(), dir1());
        testDelete(dir.createDirectory(), dir);
        testDelete(dir.createDirectory(), dir1());
    }

    private static void testDelete(
            final Resource target,
            final Resource observable) throws Exception
    {
        final boolean file = target.stat(NOFOLLOW).isRegularFile();
        try (Recorder observer = observe(observable))
        {
            final List<WatchEvent> expected = new ArrayList<>();
            // If target is file and observing on the file itself, an IN_ATTRIB
            // event is first sent in addition to IN_DELETE when deleting
            if (file && target.equals(observable))
            {
                expected.add(event(MODIFY, target));
            }
            expected.add(event(DELETE, target));
            observer.await(expected, newDelete(target));
        }
    }

    public void testDeleteRecreateDirectoryWillBeObserved() throws Exception
    {
        final Resource dir = dir1().resolve("dir");
        final Resource file = dir.resolve("file");
        try (Recorder observer = observe(dir1()))
        {
            for (int i = 0; i < 10; i++)
            {
                observer.await(CREATE, dir, newCreateDirectory(dir));
                observer.await(
                        asList(
                                event(MODIFY, dir),
                                event(MODIFY, dir),
                                event(DELETE, dir)
                        ),
                        compose(
                                newCreateFile(file),
                                newDelete(file),
                                newDelete(dir)
                        )
                );
            }
        }
    }

    public void testCreate() throws Exception
    {
        final Resource file = dir1().resolve("file");
        final Resource dir = dir1().resolve("dir");
        final Resource link = dir1().resolve("link");
        testCreateFile(file, dir1());
        testCreateDirectory(dir, dir1());
        testCreateSymbolicLink(link, dir1(), dir1());
    }

    private static void testCreateFile(
            final Resource target,
            final Resource observable) throws Exception
    {
        try (Recorder observer = observe(observable))
        {
            observer.await(CREATE, target, newCreateFile(target));
        }
    }

    private static void testCreateDirectory(
            final Resource target,
            final Resource observable) throws Exception
    {
        try (Recorder observer = observe(observable))
        {
            observer.await(CREATE, target, newCreateDirectory(target));
        }
    }

    private static void testCreateSymbolicLink(
            final Resource link,
            final Resource target,
            final Resource observable) throws Exception
    {
        try (Recorder observer = observe(observable))
        {
            observer.await(CREATE, link, newCreateSymbolicLink(link, target));
        }
    }

    public void testCreateDirectoryThenCreateItemsIntoIt() throws Exception
    {
        final Resource dir = dir1().resolve("dir");
        try (Recorder observer = observe(dir1()))
        {
            observer.await(CREATE, dir, newCreateDirectory(dir));
            observer.await(
                    asList(
                            event(MODIFY, dir),
                            event(MODIFY, dir),
                            event(MODIFY, dir)
                    ),
                    compose(
                            newCreateFile(dir.resolve("file")),
                            newCreateDirectory(dir.resolve("dir2()")),
                            newCreateSymbolicLink(dir.resolve("link"), dir1())
                    )
            );
        }
    }

    public void testCreateDirectoryThenDeleteItemsFromIt() throws Exception
    {
        final Resource parent = dir1().resolve("parent");
        final Resource file = parent.resolve("file");
        final Resource dir = parent.resolve("dir");
        try (Recorder observer = observe(dir1()))
        {
            observer.await(CREATE, parent, newCreateDirectory(parent));
            observer.await(
                    asList(
                            event(MODIFY, parent),
                            event(MODIFY, parent),
                            event(MODIFY, parent),
                            event(MODIFY, parent)
                    ),
                    compose(
                            newCreateFile(file),
                            newCreateDirectory(dir),
                            newDelete(file),
                            newDelete(dir)
                    )
            );
        }
    }

    public void testCreateDirectoryThenMoveItemsOutOfIt() throws Exception
    {
        final Resource parent = dir1().resolve("parent");
        final Resource file = parent.resolve("file");
        final Resource dir = parent.resolve("dir");
        try (Recorder observer = observe(dir1()))
        {
            observer.await(CREATE, parent, newCreateDirectory(parent));
            observer.await(
                    asList(
                            event(MODIFY, parent),
                            event(MODIFY, parent),
                            event(MODIFY, parent),
                            event(MODIFY, parent)
                    ),
                    compose(
                            newCreateFile(file),
                            newCreateDirectory(dir),
                            newMove(file, dir2().resolve("file")),
                            newMove(dir, dir2().resolve("dir"))
                    )
            );
        }
    }

    public void testCreateDirectoryThenMoveFileIntoIt() throws Exception
    {
        final Resource parent = dir1().resolve("parent");
        final Resource file = dir2().resolve("file").createFile();
        final Resource dir = dir2().resolve("dir").createDirectory();
        try (Recorder observer = observe(dir1()))
        {
            observer.await(CREATE, parent, newCreateDirectory(parent));
            observer.await(
                    asList(
                            event(MODIFY, parent),
                            event(MODIFY, parent)
                    ),
                    compose(
                            newMove(file, parent.resolve("file")),
                            newMove(dir, parent.resolve("dir"))
                    )
            );
        }
    }

    public void testMultipleOperations() throws Exception
    {
        final Resource a = dir1().resolve("a");
        final Resource b = dir1().resolve("b");
        final Resource c = dir1().resolve("c");
        final Resource d = dir1().resolve("d");
        try (Recorder observer = observe(dir1()))
        {
            observer.await(CREATE, a, newCreateDirectory(a));
            observer.await(CREATE, b, newCreateDirectory(b));
            observer.await(MODIFY, a, newCreateFile(a.resolve("1")));
            observer.await(CREATE, c, newMove(dir2().resolve("c").createFile(), c));
            observer.await(DELETE, c, newMove(c, dir2().resolve("2")));
            observer.await(DELETE, b, newDelete(b));
            observer.await(CREATE, d, newCreateFile(d));
        }
    }

    private static WatchEvent event(final Event kind, final Resource resource)
    {
        return WatchEvent.create(kind, resource);
    }

    private static Callable<Void> compose(final Callable<?>... callables)
    {
        return new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                for (final Callable<?> callable : callables)
                {
                    callable.call();
                }
                return null;
            }
        };
    }

    private static Callable<Void> newMove(final Resource src, final Resource dst)
    {
        return new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                src.moveTo(dst);
                return null;
            }
        };
    }

    private static Callable<Void> newDelete(final Resource resource)
    {
        return new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                resource.delete();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateFile(final Resource file)
    {
        return new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                file.createFile();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateDirectory(final Resource directory)
    {
        return new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                directory.createDirectory();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateSymbolicLink(
            final Resource link,
            final Resource target)
    {
        return new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                link.createLink(target);
                return null;
            }
        };
    }

    private static Callable<Void> newAppend(
            final Resource file,
            final CharSequence content)
    {
        return new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                try (OutputStream out = file.output(NOFOLLOW, true))
                {
                    out.write(content.toString().getBytes(UTF_8));
                }
                return null;
            }
        };
    }

    private static Callable<Void> newSetPermissions(
            final Resource resource,
            final Set<Permission> permissions)
    {
        return new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                resource.setPermissions(permissions);
                return null;
            }
        };
    }

    private static Callable<Void> newSetModificationTime(
            final Resource resource,
            final LinkOption option,
            final Instant instant)
    {
        return new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                resource.setModificationTime(option, instant);
                return null;
            }
        };
    }

    static final class Recorder implements Observer, Closeable
    {

        private final Resource resource;
        private Closeable subscription;
        private volatile List<WatchEvent> expected;
        private volatile List<WatchEvent> actual;
        private volatile CountDownLatch success;

        Recorder(final Resource resource)
        {
            this.resource = resource;
        }

        static Recorder observe(final Resource observable) throws IOException
        {
            return observe(observable, NOFOLLOW);
        }

        static Recorder observe(
                final Resource observable,
                final LinkOption option) throws IOException
        {
            final Recorder observer = new Recorder(observable);
            observer.subscription = observable.observe(option, observer);
            return observer;
        }

        @Override
        public void close() throws IOException
        {
            subscription.close();
        }

        @Override
        public void onEvent(final Event event, @Nullable final String child)
        {
            final Resource target = child == null
                    ? resource
                    : resource.resolve(child);
            actual.add(WatchEvent.create(event, target));
            if (expected.equals(actual))
            {
                success.countDown();
            }
        }

        void await(
                final Event kind,
                final Resource resource,
                final Callable<?> action) throws Exception
        {
            await(WatchEvent.create(kind, resource), action);
        }

        void await(
                final WatchEvent expected,
                final Callable<?> action) throws Exception
        {
            await(singletonList(expected), action);
        }

        void await(
                final List<WatchEvent> expected,
                final Callable<?> action) throws Exception
        {
            this.actual = new ArrayList<>();
            this.expected = new ArrayList<>(expected);
            this.success = new CountDownLatch(1);
            action.call();
            if (!success.await(1, SECONDS))
            {
                fail("\nexpected: " + this.expected
                        + "\nactual:   " + this.actual);
            }
        }
    }

    @AutoParcel
    static abstract class WatchEvent
    {
        abstract Event kind();

        abstract Resource resource();

        static WatchEvent create(final Event kind, final Resource resource)
        {
            return new AutoParcel_LocalResource_observe_Test_WatchEvent(
                    kind, resource
            );
        }
    }
}
