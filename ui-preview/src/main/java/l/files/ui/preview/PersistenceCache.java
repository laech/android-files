package l.files.ui.preview;

import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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

import android.support.annotation.Nullable;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.graphics.Rect;

import static android.os.AsyncTask.SERIAL_EXECUTOR;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.base.Throwables.addSuppressed;

abstract class PersistenceCache<V> extends MemCache<Path, V> {

    private final Executor loader = SERIAL_EXECUTOR;
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    private final LruCache<Path, Snapshot<V>> cache =
            new LruCache<Path, Snapshot<V>>(2000) {

                @Override
                protected void entryRemoved(
                        boolean evicted,
                        Path key,
                        Snapshot<V> oldValue,
                        Snapshot<V> newValue) {

                    super.entryRemoved(evicted, key, oldValue, newValue);
                    if (!oldValue.equals(newValue)) {
                        dirty.set(true);
                    }
                }

            };

    private static final int SUPERCLASS_VERSION = 4;

    private final Path cacheDir;
    private final int subclassVersion;

    PersistenceCache(Path cacheDir, int version) {
        this.cacheDir = requireNonNull(cacheDir);
        this.subclassVersion = version;
    }

    @Override
    Path getKey(Path path, Rect constraint) {
        return path;
    }

    @Override
    public Snapshot<V> put(Path path, Stat stat, Rect constraint, V value) {
        Snapshot<V> old = super.put(path, stat, constraint, value);
        if (old == null
                || !old.get().equals(value)
                || old.time() != stat.lastModifiedTime().to(MILLISECONDS)) {
            dirty.set(true);
        }
        return old;
    }

    @Override
    LruCache<Path, Snapshot<V>> delegate() {
        return cache;
    }

    private Path cacheFile() {
        return cacheDir.concat(cacheFileName());
    }

    abstract String cacheFileName();

    final void readAsyncIfNeeded() {
        if (loaded.get()) {
            return;
        }

        new AsyncTask<Object, Object, Object>() {
            @Nullable
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    readIfNeeded();
                } catch (IOException e) {
                    Log.w(PersistenceCache.this.getClass().getSimpleName(),
                            "Failed to read cache.", e);
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
        DataInputStream in = null;
        try {

            in = newBufferedDataInputStream(file);
            if (in.readInt() != SUPERCLASS_VERSION) {
                return;
            }
            if (in.readInt() != subclassVersion) {
                return;
            }

            while (true) {
                try {

                    in.readUTF(); // For backward compatibility
                    short len = in.readShort();
                    byte[] bytes = new byte[len];
                    in.readFully(bytes);
                    Path key = Path.of(bytes); // TODO
                    long time = in.readLong();
                    V value = read(in);

                    cache.put(key, Snapshot.of(value, time));

                } catch (EOFException e) {
                    break;
                }
            }

        } catch (FileNotFoundException ignore) {
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private DataInputStream newBufferedDataInputStream(Path path) throws IOException {
        return new DataInputStream(new BufferedInputStream(path.newInputStream()));
    }

    abstract V read(DataInput in) throws IOException;

    final void writeAsyncIfNeeded() {
        if (!dirty.get()) {
            return;
        }

        new AsyncTask<Object, Object, Object>() {
            @Nullable
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    writeIfNeeded();
                } catch (IOException e) {
                    Log.w(PersistenceCache.this.getClass().getSimpleName(),
                            "Failed to write cache.", e);
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
        assert parent != null;
        parent.createDirectories();

        Path tmp = parent.concat(file.name() + "-" + nanoTime());
        try (DataOutputStream out = newBufferedDataOutputStream(tmp)) {

            out.writeInt(SUPERCLASS_VERSION);
            out.writeInt(subclassVersion);

            Map<Path, Snapshot<V>> snapshot = cache.snapshot();
            for (Map.Entry<Path, Snapshot<V>> entry : snapshot.entrySet()) {

                byte[] bytes = entry.getKey().toByteArray();
                out.writeUTF(""); // For backward compatibility
                out.writeShort(bytes.length);
                out.write(bytes);
                out.writeLong(entry.getValue().time());
                write(out, entry.getValue().get());
            }

        } catch (Exception e) {
            try {
                tmp.delete();
            } catch (IOException sup) {
                addSuppressed(e, sup);
            }
            throw e;
        }

        tmp.rename(file);
    }

    private DataOutputStream newBufferedDataOutputStream(Path path) throws IOException {
        return new DataOutputStream(new BufferedOutputStream(path.newOutputStream(false)));
    }

    abstract void write(DataOutput out, V value) throws IOException;

}
