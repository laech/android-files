package l.files.ui.preview;

import android.os.AsyncTask;
import android.support.v4.util.LruCache;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import l.files.base.io.Closer;
import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Stat;

import static android.os.AsyncTask.SERIAL_EXECUTOR;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.base.Throwables.addSuppressed;

abstract class PersistenceCache<V> extends MemCache<V> {

    private final Executor loader = SERIAL_EXECUTOR;
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    private final LruCache<ByteBuffer, Snapshot<V>> cache =
            new LruCache<ByteBuffer, Snapshot<V>>(5000) {

                @Override
                protected void entryRemoved(
                        boolean evicted,
                        ByteBuffer key,
                        Snapshot<V> oldValue,
                        Snapshot<V> newValue) {

                    super.entryRemoved(evicted, key, oldValue, newValue);
                    if (!oldValue.equals(newValue)) {
                        dirty.set(true);
                    }
                }

            };

    private static final byte SUPERCLASS_VERSION = 3;

    private final Path cacheDir;
    private final byte subclassVersion;

    PersistenceCache(Path cacheDir, byte version) {
        this.cacheDir = requireNonNull(cacheDir);
        this.subclassVersion = version;
    }

    @Override
    final void key(ByteBuffer key, Path path, Stat stat, Rect constraint) {
        path.toByteArray(key.asOutputStream());
    }

    @Override
    Snapshot<V> put(Path path, Stat stat, Rect constraint, V value) {
        Snapshot<V> old = super.put(path, stat, constraint, value);
        if (old == null
                || !old.get().equals(value)
                || old.time() != stat.lastModifiedTime().to(MILLISECONDS)) {
            dirty.set(true);
        }
        return old;
    }

    @Override
    LruCache<ByteBuffer, Snapshot<V>> delegate() {
        return cache;
    }

    private Path cacheFile() {
        return cacheDir.resolve(cacheFileName());
    }

    abstract String cacheFileName();

    final void readAsyncIfNeeded() {
        if (loaded.get()) {
            return;
        }

        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    readIfNeeded();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(loader);
    }

    final void readIfNeeded() throws IOException {
        if (!loaded.compareAndSet(false, true)) {
            return;
        }

        Path file = cacheFile();
        Closer closer = Closer.create();
        try {

            DataInputStream in = closer.register(Files.newBufferedDataInputStream(file));

            if (in.readByte() != SUPERCLASS_VERSION) {
                return;
            }
            if (in.readByte() != subclassVersion) {
                return;
            }

            while (true) {
                try {

                    ByteBuffer key = ByteBuffer.readFrom(in);
                    long time = in.readLong();
                    V value = read(in);

                    cache.put(key, Snapshot.of(value, time));

                } catch (EOFException e) {
                    break;
                }
            }

        } catch (FileNotFoundException ignore) {
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    abstract V read(DataInput in) throws IOException;

    final void writeAsyncIfNeeded() {
        if (!dirty.get()) {
            return;
        }

        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    writeIfNeeded();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(loader);
    }

    final void writeIfNeeded() throws IOException {
        if (!dirty.compareAndSet(true, false)) {
            return;
        }

        Path file = cacheFile();
        Path parent = file.parent();
        Files.createDirs(parent);

        Path tmp = parent.resolve(file.name() + "-" + nanoTime());
        Closer closer = Closer.create();
        try {

            DataOutputStream out = closer.register(Files.newBufferedDataOutputStream(tmp));
            out.writeByte(SUPERCLASS_VERSION);
            out.writeByte(subclassVersion);

            Map<ByteBuffer, Snapshot<V>> snapshot = cache.snapshot();
            for (Map.Entry<ByteBuffer, Snapshot<V>> entry : snapshot.entrySet()) {

                entry.getKey().writeTo(out);
                out.writeLong(entry.getValue().time());
                write(out, entry.getValue().get());
            }

        } catch (Exception e) {
            try {
                Files.delete(tmp);
            } catch (IOException sup) {
                addSuppressed(e, sup);
            }
            throw e;
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }

        Files.move(tmp, file);
    }

    abstract void write(DataOutput out, V value) throws IOException;

}
