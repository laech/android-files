package l.files.ui.preview;

import android.content.Context;
import android.os.AsyncTask;
import android.util.LruCache;

import com.google.common.base.Stopwatch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import l.files.common.graphics.Rect;
import l.files.fs.Instant;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.logging.Logger;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static l.files.common.base.Stopwatches.startWatchIfDebug;

abstract class PersistenceCache<V> extends MemCache<V> {

  // TODO no action for files in cache dir

  private final Logger log = Logger.get(getClass());

  private final Executor loader = newSingleThreadExecutor();
  private final AtomicBoolean loaded = new AtomicBoolean(false);
  private final AtomicBoolean dirty = new AtomicBoolean(false);

  private final LruCache<String, Snapshot<V>> cache =
      new LruCache<String, Snapshot<V>>(5000) {

        @Override protected void entryRemoved(
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

  @Override String key(Resource res, Stat stat, Rect constraint) {
    return res.scheme() + "_" + res.path();
  }

  @Override Snapshot<V> put(Resource res, Stat stat, Rect constraint, V value) {
    Snapshot<V> old = super.put(res, stat, constraint, value);
    if (old == null
        || !old.get().equals(value)
        || !old.time().equals(stat.mtime())) {
      dirty.set(true);
    }
    return old;
  }

  @Override LruCache<String, Snapshot<V>> delegate() {
    return cache;
  }

  private File cacheFile(Context context) {
    return new File(context.getExternalCacheDir(), cacheFileName());
  }

  abstract String cacheFileName();

  final void readAsyncIfNeeded(final Context context) {
    if (loaded.get()) {
      return;
    }

    new AsyncTask<Object, Object, Object>() {
      @Override protected Object doInBackground(Object... params) {
        readIfNeeded(context);
        return null;
      }
    }.executeOnExecutor(loader);
  }

  final void readIfNeeded(Context context) {
    if (!loaded.compareAndSet(false, true)) {
      return;
    }

    File file = cacheFile(context);
    try (DataInputStream in =
             new DataInputStream(
                 new BufferedInputStream(
                     new FileInputStream(file)))) {

      Stopwatch watch = startWatchIfDebug();
      int count = 0;
      while (true) {
        try {

          String key = in.readUTF();
          long seconds = in.readLong();
          int nanos = in.readInt();
          Instant time = Instant.of(seconds, nanos);
          V value = read(context, in);
          cache.put(key, Snapshot.of(value, time));

          count++;

        } catch (EOFException e) {
          break;
        }
      }
      log.debug("read cache %s %s entries", watch, count);

    } catch (FileNotFoundException ignore) {
    } catch (IOException e) {
      log.error(e);
    }
  }

  abstract V read(Context context, DataInput in) throws IOException;

  final void writeAsyncIfNeeded(final Context context) {
    if (!dirty.get()) {
      return;
    }

    new AsyncTask<Object, Object, Object>() {
      @Override protected Object doInBackground(Object... params) {
        writeIfNeeded(context);
        return null;
      }
    }.executeOnExecutor(loader);
  }

  final void writeIfNeeded(Context context) {
    if (!dirty.compareAndSet(true, false)) {
      return;
    }

    File file = cacheFile(context);
    file.getParentFile().mkdirs();
    try (DataOutputStream out =
             new DataOutputStream(
                 new BufferedOutputStream(
                     new FileOutputStream(file)))) {

      Stopwatch watch = startWatchIfDebug();
      Map<String, Snapshot<V>> snapshot = cache.snapshot();
      for (Map.Entry<String, Snapshot<V>> entry : snapshot.entrySet()) {
        out.writeUTF(entry.getKey());
        out.writeLong(entry.getValue().time().seconds());
        out.writeInt(entry.getValue().time().nanos());
        write(out, entry.getValue().get());
      }
      log.debug("write cache %s %s entries", watch, snapshot.size());

    } catch (IOException e) {
      log.error(e);
    }
  }

  abstract void write(DataOutput out, V value) throws IOException;

}
