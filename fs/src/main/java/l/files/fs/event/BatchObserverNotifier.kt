package l.files.fs.event

import l.files.fs.Observable
import java.io.IOException
import java.lang.System.nanoTime
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.util.concurrent.Executors.newSingleThreadScheduledExecutor
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

private val service = newSingleThreadScheduledExecutor {
  Thread(it, "BatchObserverNotifier")
}

class BatchObserverNotifier(
  private val batchObserver: BatchObserver,
  private val batchInterval: Long,
  private val batchInternalUnit: TimeUnit,
  /**
   * If true, will deliver the new event immediate instead of waiting for next
   * schedule check to run, if the previous event was a while ago or there was
   * no previous event.
   */
  private val quickNotifyFirstEvent: Boolean
) : Observer, Observation, Runnable {

  private val childFileNameChanged = HashMap<Path, WatchEvent.Kind<*>>()
  private val batchIntervalNanos = batchInternalUnit.toNanos(batchInterval)

  @Volatile
  private var observation: Observation? = null

  @Volatile
  private var checker: ScheduledFuture<*>? = null
  private val started = AtomicBoolean(false)

  @Volatile
  private var quickNotifyLastRunNanos: Long = 0

  @Throws(IOException::class)
  fun start(path: Path, childrenConsumer: Consumer<Path>): Observation {
    check(started.compareAndSet(false, true))

    try {
      val ob = Observable(path, this, childrenConsumer)
      ob.start()
      observation = ob
      if (!ob.isClosed) {
        checker = service.scheduleWithFixedDelay(
          this, batchInterval, batchInterval, batchInternalUnit
        )
      }
    } catch (e: Throwable) {
      try {
        close()
      } catch (sup: Exception) {
        e.addSuppressed(sup)
      }
      throw e
    }
    return this
  }

  override fun onEvent(kind: WatchEvent.Kind<*>, childFileName: Path) {
    synchronized(this) {
      childFileNameChanged.put(childFileName, kind)
    }
    if (quickNotifyFirstEvent) {
      val now = nanoTime()
      if (now - quickNotifyLastRunNanos > batchIntervalNanos) {
        quickNotifyLastRunNanos = now
        service.execute(this)
      }
    }
  }

  override fun onIncompleteObservation(cause: IOException) {
    batchObserver.onIncompleteObservation(cause)
  }

  override fun run() {
    if (quickNotifyFirstEvent) {
      quickNotifyLastRunNanos = nanoTime()
    }

    var snapshotChildFileNameChanged: Map<Path, WatchEvent.Kind<*>>
    synchronized(this) {
      snapshotChildFileNameChanged = childFileNameChanged.toMap()
      childFileNameChanged.clear()
    }
    if (snapshotChildFileNameChanged.isNotEmpty()) {
      batchObserver.onLatestEvents(snapshotChildFileNameChanged)
    }
  }

  @Throws(IOException::class)
  override fun close() {
    checker?.cancel(true)
    observation?.close()
  }

  override val isClosed get() = observation?.isClosed ?: true

  override fun closeReason() = observation?.closeReason()
}
