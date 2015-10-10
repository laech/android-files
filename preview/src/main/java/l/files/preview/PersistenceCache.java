package l.files.preview;

import android.os.AsyncTask;
import android.util.LruCache;

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
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.Stat;

import static android.os.AsyncTask.SERIAL_EXECUTOR;
import static java.util.Objects.requireNonNull;

abstract class PersistenceCache<V> extends MemCache<V> {

    private final Executor loader = SERIAL_EXECUTOR;
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    private final LruCache<String, Snapshot<V>> cache =
            new LruCache<String, Snapshot<V>>(5000) {

                @Override
                protected void entryRemoved(
                        boolean evicted,
                        String key,
                        Snapshot<V> oldValue,
                        Snapshot<V> newValue) {

                    super.entryRemoved(evicted, key, oldValue, newValue);
                    if (!Objects.equals(oldValue, newValue)) {
                        dirty.set(true);
                    }
                }

            };

    private final File cacheDir;

    PersistenceCache(File cacheDir) {
        this.cacheDir = requireNonNull(cacheDir);
    }

    @Override
    String key(File res, Stat stat, Rect constraint) {
        return res.scheme() + "_" + res.path();
    }

    @Override
    Snapshot<V> put(File res, Stat stat, Rect constraint, V value) {
        Snapshot<V> old = super.put(res, stat, constraint, value);
        if (old == null
                || !old.get().equals(value)
                || !old.time().equals(stat.lastModifiedTime())) {
            dirty.set(true);
        }
        return old;
    }

    @Override
    LruCache<String, Snapshot<V>> delegate() {
        return cache;
    }

    private File cacheFile() {
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
                readIfNeeded();
                return null;
            }
        }.executeOnExecutor(loader);
    }

    final void readIfNeeded() {
        if (!loaded.compareAndSet(false, true)) {
            return;
        }

        File file = cacheFile();
        try (DataInputStream in = file.newBufferedDataInputStream()) {

            while (true) {
                try {

                    String key = in.readUTF();
                    long seconds = in.readLong();
                    int nanos = in.readInt();
                    Instant time = Instant.of(seconds, nanos);
                    V value = read(in);
                    cache.put(key, Snapshot.of(value, time));

                } catch (EOFException e) {
                    break;
                }
            }

        } catch (FileNotFoundException ignore) {
        } catch (IOException e) {
            e.printStackTrace();
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
                writeIfNeeded();
                return null;
            }
        }.executeOnExecutor(loader);
    }

    final void writeIfNeeded() {
        if (!dirty.compareAndSet(true, false)) {
            return;
        }

        File file = cacheFile();
        try {
            file.createFiles();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (DataOutputStream out = file.newBufferedDataOutputStream()) {

            Map<String, Snapshot<V>> snapshot = cache.snapshot();
            for (Map.Entry<String, Snapshot<V>> entry : snapshot.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeLong(entry.getValue().time().seconds());
                out.writeInt(entry.getValue().time().nanos());
                write(out, entry.getValue().get());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    abstract void write(DataOutput out, V value) throws IOException;

}
