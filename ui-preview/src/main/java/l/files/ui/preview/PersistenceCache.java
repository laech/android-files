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
import l.files.fs.File;
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

    private final LruCache<String, Snapshot<V>> cache =
            new LruCache<String, Snapshot<V>>(5000) {

                @Override
                protected void entryRemoved(
                        boolean evicted,
                        String key,
                        Snapshot<V> oldValue,
                        Snapshot<V> newValue) {

                    super.entryRemoved(evicted, key, oldValue, newValue);
                    if (!oldValue.equals(newValue)) {
                        dirty.set(true);
                    }
                }

            };

    private static final byte SUPERCLASS_VERSION = 0;

    private final File cacheDir;
    private final byte subclassVersion;

    PersistenceCache(File cacheDir, byte version) {
        this.cacheDir = requireNonNull(cacheDir);
        this.subclassVersion = version;
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
                || old.time() != stat.lastModifiedTime().to(MILLISECONDS)) {
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

        File file = cacheFile();
        Closer closer = Closer.create();
        try {

            DataInputStream in = closer.register(file.newBufferedDataInputStream());

            if (in.readByte() != SUPERCLASS_VERSION) {
                return;
            }
            if (in.readByte() != subclassVersion) {
                return;
            }

            while (true) {
                try {

                    String key = in.readUTF();
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

        File file = cacheFile();
        File parent = file.parent();
        parent.createDirs();

        File tmp = parent.resolve(file.name() + "-" + nanoTime());
        Closer closer = Closer.create();
        try {

            DataOutputStream out = closer.register(tmp.newBufferedDataOutputStream());
            out.writeByte(SUPERCLASS_VERSION);
            out.writeByte(subclassVersion);

            Map<String, Snapshot<V>> snapshot = cache.snapshot();
            for (Map.Entry<String, Snapshot<V>> entry : snapshot.entrySet()) {
                out.writeUTF(entry.getKey());
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
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }

        tmp.moveTo(file);
    }

    abstract void write(DataOutput out, V value) throws IOException;

}
