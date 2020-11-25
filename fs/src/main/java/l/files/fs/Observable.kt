package l.files.fs

import android.util.Log
import l.files.fs.event.Observation
import l.files.fs.event.Observer
import java.io.IOException
import java.nio.file.ClosedWatchServiceException
import java.nio.file.Files.isDirectory
import java.nio.file.Files.list
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

internal class Observable(
  private val dir: Path,
  private val observer: Observer,
  private val childConsumer: Consumer<Path>
) : Observation, Runnable {

  private var closed = AtomicBoolean(false)
  private var watchService: WatchService? = null
  private val watchKeyToChildFileName = HashMap<WatchKey, Path>()

  override val isClosed get() = closed.get()

  override fun closeReason() = null

  override fun close() {
    if (closed.compareAndSet(false, true)) {
      watchService?.close()
    }
  }

  @Throws(IOException::class)
  fun start() {
    watchService = dir.fileSystem.newWatchService()
    dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
    Thread(this).start()
    list(dir).use {
      it.forEach { child ->
        if (isDirectory(child)) {
          try {
            watchKeyToChildFileName[child.register(
              watchService,
              ENTRY_CREATE,
              ENTRY_DELETE
            )] = child.fileName
          } catch (e: IOException) {
            Log.d(javaClass.simpleName, "", e)
          }
        }
        childConsumer.accept(child)
      }
    }
  }

  override fun run() {
    try {
      while (true) {
        val watchKey = watchService!!.take()
        for (event in watchKey.pollEvents()) {
          if (event.kind() == OVERFLOW) {
            observer.onIncompleteObservation(IOException("Overflow"))
            return
          }
          observer.onEvent(
            event.kind(),
            watchKeyToChildFileName[watchKey] ?: event.context() as Path
          )
        }
        watchKey.reset()
      }
    } catch (e: ClosedWatchServiceException) {
    } catch (e: InterruptedException) {
    }
  }
}
