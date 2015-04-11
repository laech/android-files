package l.files.fs.local;

import com.google.common.collect.ImmutableSet;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import l.files.fs.Path;
import l.files.fs.Resource;
import l.files.fs.UncheckedIOException;
import l.files.fs.WatchService;
import l.files.logging.Logger;

import static l.files.fs.WatchEvent.Listener;

@Deprecated
public class LocalWatchService implements WatchService, Closeable {

  /*
    Note:
    Two FileObserver instances cannot be monitoring on the same inode, if one is
    stopped, the other will also be stopped, because FileObserver internally
    uses global states.
   */

    private static final Logger logger = Logger.get(LocalWatchService.class);

    /**
     * System directories such as /dev, /proc contain special files (some aren't
     * really files), they generate tons of file system events (MODIFY etc) and
     * they don't change. WatchService should not allow them and their sub paths
     * to be watched.
     */
    static final ImmutableSet<Path> IGNORED = ImmutableSet.<Path>of(
            LocalPath.of("/sys"),
            LocalPath.of("/proc"),
            LocalPath.of("/dev")
    );

    private static final LocalWatchService INSTANCE = new LocalWatchService(IGNORED) {
        @Override
        public void close() {
            throw new UnsupportedOperationException("Can't close shared instance");
        }
    };

    public static LocalWatchService create() {
        return new LocalWatchService(IGNORED);
    }

    /**
     * Gets a shared instance. The return instance cannot be closed.
     */
    public static LocalWatchService get() {
        return INSTANCE;
    }

    private final Path[] ignored;

    private final Map<Listener, Closeable> observables = new HashMap<>();

    LocalWatchService(Set<Path> ignored) {
        this.ignored = ignored.toArray(new Path[ignored.size()]);
    }

    @Override
    public void register(Resource resource, Listener listener) throws IOException {
        if (!isWatchable(resource)) {
            return;
        }

        synchronized (this) {
            Closeable observable = observables.get(listener);
            if (observable != null) {
                throw new IllegalArgumentException(listener.toString()); // TODO
            }
            observables.put(listener, LocalResourceObservable.observe((LocalResource) resource, listener));
        }
    }


    @Override
    public void unregister(Resource resource, Listener listener) {
        synchronized (this) {

            Closeable observable = observables.remove(listener);
            if (observable != null) {
                try {
                    observable.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e); // TODO
                }
            }
        }
    }

    @Override
    public boolean isWatchable(Resource resource) {
        for (Path unwatchable : ignored) {
            if (resource.getPath().startsWith(unwatchable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void close() {
        synchronized (this) {
            for (Closeable observable : observables.values()) {
                try {
                    observable.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
            observables.clear();
        }
    }

}
