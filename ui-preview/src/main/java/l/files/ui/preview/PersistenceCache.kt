package l.files.ui.preview

import android.os.AsyncTask
import android.util.Log
import androidx.collection.LruCache
import l.files.base.Throwables
import l.files.fs.Path
import l.files.fs.Stat
import l.files.fs.newBufferedDataInputStream
import l.files.fs.newBufferedDataOutputStream
import l.files.ui.base.graphics.Rect
import java.io.*
import java.lang.System.nanoTime
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicBoolean

private const val SUPERCLASS_VERSION = 6

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
    stat: Stat,
    constraint: Rect,
    value: V
  ): Snapshot<V>? {
    val old = super.put(path, stat, constraint, value)
    if (old == null
      || old.value != value
      || old.time != stat.lastModifiedTime().to(MILLISECONDS)
    ) {
      dirty.set(true)
    }
    return old
  }

  private fun cacheFile(): Path = cacheDir.concat(cacheFileName)

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
      file.newBufferedDataInputStream().use {
        if (it.readInt() != SUPERCLASS_VERSION) return
        if (it.readInt() != subclassVersion) return

        while (true) {
          try {
            val len = it.readShort()
            val bytes = ByteArray(len.toInt())
            it.readFully(bytes)
            val key = Path.of(bytes)
            val time = it.readLong()
            val value = read(it)
            delegate.put(key, Snapshot(value, time))
          } catch (e: EOFException) {
            break
          }
        }
      }
    } catch (ignore: FileNotFoundException) {
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
          "Failed to write cache.", e
        )
      }
    }
  }

  fun writeIfNeeded() {
    if (!dirty.compareAndSet(true, false)) {
      return
    }
    val file = cacheFile()
    val parent = file.parent()!!
    parent.createDirectories()
    val tmp = parent.concat("${file.fileName}-${nanoTime()}")
    try {
      tmp.newBufferedDataOutputStream(false).use {
        it.writeInt(SUPERCLASS_VERSION)
        it.writeInt(subclassVersion)
        val snapshot = delegate.snapshot()
        for ((key, value) in snapshot) {
          val bytes = key.toByteArray()
          it.writeShort(bytes.size)
          it.write(bytes)
          it.writeLong(value.time)
          write(it, value.value)
        }
      }
    } catch (e: Exception) {
      try {
        tmp.delete()
      } catch (sup: IOException) {
        Throwables.addSuppressed(e, sup)
      }
      throw e
    }
    tmp.rename(file)
  }

  abstract fun write(out: DataOutput, value: V)

}
