package l.files.fs.event

import android.util.Log
import java.io.Closeable
import java.io.IOException
import java.nio.file.ClosedWatchServiceException
import java.nio.file.Files.isDirectory
import java.nio.file.Files.list
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.util.function.Consumer

internal class Observable(
  private val dir: Path,
  private val observer: Observer,
  private val childConsumer: Consumer<Path>
) : Closeable, Runnable {

  private var watchService: WatchService? = null
  private val watchKeyToChildFileName = HashMap<WatchKey, Path>()

  override fun close() {
    watchService?.close()
  }

  @Throws(IOException::class)
  fun start(): Observable {
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
    return this
  }

  override fun run() {
    try {
      while (true) {
        val watchKey = watchService!!.take()
        for (event in watchKey.pollEvents()) {
          if (event.kind() == OVERFLOW) {
            Log.d(javaClass.simpleName, "Event overflowed.")
            close()
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
