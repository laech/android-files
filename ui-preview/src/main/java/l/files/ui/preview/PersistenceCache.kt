package l.files.ui.preview

import android.os.AsyncTask
import android.util.Log
import androidx.collection.LruCache
import l.files.base.Throwables
import l.files.ui.base.graphics.Rect
import java.io.*
import java.lang.System.nanoTime
import java.nio.file.Files.*
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.time.DateTimeException
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

private const val SUPERCLASS_VERSION = 8

internal abstract class PersistenceCache<V>(
  cacheDir: () -> Path,
  private val subclassVersion: Int
) : MemCache<Path, V>() {

  private val loader = AsyncTask.SERIAL_EXECUTOR
  private val loaded = AtomicBoolean(false)
  private val dirty = AtomicBoolean(false)

  override val delegate = object : LruCache<Path, Snapshot<V>>(2000) {
    override fun entryRemoved(
      evicted: Boolean,
      key: Path,
      oldValue: Snapshot<V>,
      newValue: Snapshot<V>?
    ) {
      super.entryRemoved(evicted, key, oldValue, newValue)
      if (oldValue != newValue) {
        dirty.set(true)
      }
    }
  }

  private val cacheDir by lazy { cacheDir() }

  override fun getKey(path: Path, constraint: Rect): Path = path

  override fun put(
    path: Path,
    time: Instant,
    constraint: Rect,
    value: V
  ): Snapshot<V>? {
    val old = super.put(path, time, constraint, value)
    if (old == null || old.value != value || old.time != time) {
      dirty.set(true)
    }
    return old
  }

  private fun cacheFile(): Path = cacheDir.resolve(cacheFileName)

  abstract val cacheFileName: String

  fun readAsyncIfNeeded() {
    if (loaded.get()) {
      return
    }
    loader.execute {
      try {
        readIfNeeded()
      } catch (e: IOException) {
        Log.w(
          this@PersistenceCache.javaClass.simpleName,
          "Failed to read cache.",
          e
        )
      }
    }
  }

  fun readIfNeeded() {
    if (!loaded.compareAndSet(false, true)) {
      return
    }
    val file = cacheFile()
    try {
      DataInputStream(newInputStream(file).buffered()).use {
        if (it.readInt() != SUPERCLASS_VERSION) return
        if (it.readInt() != subclassVersion) return

        while (true) {
          try {
            val key = Paths.get(it.readUTF())
            val second = it.readLong()
            val nano = it.readInt()
            val time = Instant.ofEpochSecond(second, nano.toLong())
            val value = read(it)
            delegate.put(key, Snapshot(value, time))
          } catch (e: DateTimeException) {
            Log.d(this@PersistenceCache.javaClass.simpleName, "", e)
          } catch (e: EOFException) {
            break
          }
        }
      }
    } catch (ignore: FileNotFoundException) {
    } catch (ignore: NoSuchFileException) {
    }
  }

  abstract fun read(input: DataInput): V

  fun writeAsyncIfNeeded() {
    if (!dirty.get()) {
      return
    }
    loader.execute {
      try {
        writeIfNeeded()
      } catch (e: IOException) {
        Log.w(
          this@PersistenceCache.javaClass.simpleName,
          "Failed to write cache.",
          e
        )
      }
    }
  }

  fun writeIfNeeded() {
    if (!dirty.compareAndSet(true, false)) {
      return
    }
    val file = cacheFile()
    val parent = file.parent!!
    createDirectories(parent)
    val tmp = parent.resolve("${file.fileName}-${nanoTime()}")
    try {
      DataOutputStream(newOutputStream(tmp).buffered()).use {
        it.writeInt(SUPERCLASS_VERSION)
        it.writeInt(subclassVersion)
        val snapshot = delegate.snapshot()
        for ((key, value) in snapshot) {
          it.writeUTF(key.toString())
          it.writeLong(value.time.epochSecond)
          it.writeInt(value.time.nano)
          write(it, value.value)
        }
      }
    } catch (e: Exception) {
      try {
        delete(tmp)
      } catch (sup: IOException) {
        Throwables.addSuppressed(e, sup)
      }
      throw e
    }
    move(tmp, file, REPLACE_EXISTING)
  }

  abstract fun write(out: DataOutput, value: V)

}
