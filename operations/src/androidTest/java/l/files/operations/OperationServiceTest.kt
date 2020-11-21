package l.files.operations

import android.content.ComponentName
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import l.files.testing.fs.PathBaseTest
import l.files.testing.fs.Paths.createFiles
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.nio.file.Files.*
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class OperationServiceTest : PathBaseTest() {

  private lateinit var context: Context
  private lateinit var listener: TaskListener
  private lateinit var service: OperationService

  override fun setUp() {
    super.setUp()
    context = InstrumentationRegistry.getInstrumentation().context
    service = OperationService(false) { listener }
  }

  private fun <T : TaskListener> setListener(listener: T): T {
    this.listener = listener
    return listener
  }

  @Test
  fun cancel_intent() {
    val intent = newCancelIntent(context, 101)
    assertEquals(ACTION_CANCEL, intent.action)
    assertEquals(
      101,
      intent.getIntExtra(EXTRA_TASK_ID, -1).toLong()
    )
    assertEquals(
      ComponentName(context, OperationService::class.java),
      intent.component
    )
  }

  @Test
  fun cancel_task_not_found() {
    listener = mock(TaskListener::class.java)
    service.onCreate()
    service.onStartCommand(newCancelIntent(context, 1011), 0, 0)
    verify(listener).onNotFound(service, TaskNotFound.create(1011))
  }

  @Test
  fun moves_file() {
    val src = createFile(dir1().resolve("a"))
    val dst = createDirectory(dir1().resolve("dst"))
    val listener = setListener(CountDownListener(TaskKind.MOVE))
    service.onCreate()
    service.onStartCommand(newMoveIntent(context, setOf(src), dst), 0, 0)
    listener.await()
    assertThat(exists(src, NOFOLLOW_LINKS), equalTo(false))
    assertThat(
      exists(dst.resolve(src.fileName), NOFOLLOW_LINKS),
      equalTo(true)
    )
  }

  @Test
  fun copies_file() {
    val src = createFile(dir1().resolve("a"))
    val dst = createDirectory(dir1().resolve("dst"))
    val listener = setListener(CountDownListener(TaskKind.COPY))
    service.onCreate()
    service.onStartCommand(newCopyIntent(context, setOf(src), dst), 0, 0)
    listener.await()
    assertThat(exists(src, NOFOLLOW_LINKS), equalTo(true))
    assertThat(
      exists(dst.resolve(src.fileName), NOFOLLOW_LINKS),
      equalTo(true)
    )
  }

  @Test
  fun deletes_files() {
    val a = dir1().resolve("a")
    val b = dir1().resolve("b/c")
    createFiles(a)
    createFiles(b)
    val listener = setListener(CountDownListener(TaskKind.DELETE))
    service.onCreate()
    service.onStartCommand(newDeleteIntent(context, listOf(a, b)), 0, 0)
    listener.await()
    assertThat(exists(a, NOFOLLOW_LINKS), equalTo(false))
    assertThat(exists(b, NOFOLLOW_LINKS), equalTo(false))
  }

  @Test
  fun task_start_time_is_correct() {
    val file1 = createFile(dir1().resolve("a"))
    val file2 = createFile(dir1().resolve("b"))
    val listener = setListener(CountDownListener(TaskKind.DELETE))
    service.onCreate()
    val start = System.currentTimeMillis()
    service.onStartCommand(newDeleteIntent(context, listOf(file1, file2)), 0, 0)
    listener.await()
    val end = System.currentTimeMillis()
    val times = getTaskStartTimes(listener.values)
    assertThat(times.iterator().next() >= start, equalTo(true))
    assertThat(times.iterator().next() <= end, equalTo(true))
  }

  private fun getTaskStartTimes(values: List<TaskState>): Set<Long> =
    values.asSequence().map { it.time().time() }.toSet()

  private class CountDownListener(
    private val kind: TaskKind,
    countDowns: Int = 1
  ) : TaskListener {

    private val latch: CountDownLatch = CountDownLatch(countDowns)

    val values = ArrayList<TaskState>(countDowns)

    override fun onUpdate(context: Context, state: TaskState) {
      values.add(state)
      assertEquals(kind, state.task().kind())
      if (state.isFinished) {
        latch.countDown()
      }
    }

    fun await() {
      assertThat(latch.await(1, TimeUnit.SECONDS), equalTo(true))
    }

    override fun onNotFound(context: Context, notFound: TaskNotFound) {}
  }
}
