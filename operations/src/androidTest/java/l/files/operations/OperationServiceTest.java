package l.files.operations;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import l.files.fs.Path;
import l.files.operations.OperationService.TaskListener;
import l.files.testing.fs.PathBaseTest;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.Files.createDir;
import static l.files.fs.Files.createFile;
import static l.files.fs.Files.createFiles;
import static l.files.fs.Files.exists;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.operations.OperationService.ACTION_CANCEL;
import static l.files.operations.OperationService.EXTRA_TASK_ID;
import static l.files.operations.OperationService.newCancelIntent;
import static l.files.operations.OperationService.newCopyIntent;
import static l.files.operations.OperationService.newDeleteIntent;
import static l.files.operations.OperationService.newMoveIntent;
import static l.files.operations.TaskKind.COPY;
import static l.files.operations.TaskKind.DELETE;
import static l.files.operations.TaskKind.MOVE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class OperationServiceTest extends PathBaseTest {

    private OperationService service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        service = new OperationService();
        service.foreground = false;
    }

    public void test_cancel_intent() throws Exception {

        Intent intent = newCancelIntent(getContext(), 101);
        assertEquals(ACTION_CANCEL, intent.getAction());
        assertEquals(101, intent.getIntExtra(EXTRA_TASK_ID, -1));
        assertEquals(new ComponentName(getContext(), OperationService.class),
                intent.getComponent());
    }

    public void test_cancel_task_not_found() throws Exception {

        TaskListener listener = mock(TaskListener.class);
        service.listener = listener;
        service.onCreate();

        service.onStartCommand(newCancelIntent(getContext(), 1011), 0, 0);

        verify(listener).onNotFound(service, TaskNotFound.create(1011));
    }

    public void test_moves_file() throws Exception {

        Path src = createFile(dir1().concat("a"));
        Path dst = createDir(dir1().concat("dst"));
        CountDownListener listener = new CountDownListener(MOVE);
        service.listener = listener;
        service.onCreate();

        service.onStartCommand(newMoveIntent(getContext(), singleton(src), dst), 0, 0);

        listener.await();
        assertFalse(exists(src, NOFOLLOW));
        assertTrue(exists(dst.concat(src.name()), NOFOLLOW));
    }

    public void test_copies_file() throws Exception {

        Path src = createFile(dir1().concat("a"));
        Path dst = createDir(dir1().concat("dst"));
        CountDownListener listener = new CountDownListener(COPY);
        service.listener = listener;
        service.onCreate();

        service.onStartCommand(newCopyIntent(getContext(), singleton(src), dst), 0, 0);

        listener.await();
        assertTrue(exists(src, NOFOLLOW));
        assertTrue(exists(dst.concat(src.name()), NOFOLLOW));
    }

    public void test_deletes_files() throws Exception {

        Path a = createFiles(dir1().concat("a"));
        Path b = createFiles(dir1().concat("b/c"));
        CountDownListener listener = new CountDownListener(DELETE);
        service.listener = listener;
        service.onCreate();

        service.onStartCommand(newDeleteIntent(getContext(), asList(a, b)), 0, 0);

        listener.await();
        assertFalse(exists(a, NOFOLLOW));
        assertFalse(exists(b, NOFOLLOW));
    }

    public void test_task_start_time_is_correct() throws Exception {

        Path file1 = createFile(dir1().concat("a"));
        Path file2 = createFile(dir1().concat("b"));
        CountDownListener listener = new CountDownListener(DELETE);
        service.listener = listener;
        service.onCreate();

        long start = currentTimeMillis();
        service.onStartCommand(newDeleteIntent(getContext(), asList(file1, file2)), 0, 0);
        listener.await();
        long end = currentTimeMillis();

        Set<Long> times = getTaskStartTimes(listener.getValues());
        assertTrue(times.iterator().next() >= start);
        assertTrue(times.iterator().next() <= end);
    }

    private Set<Long> getTaskStartTimes(List<? extends TaskState> values) {
        Set<Long> result = new HashSet<>();
        for (TaskState value : values) {
            result.add(value.time().time());
        }
        return result;
    }

    private static class CountDownListener implements TaskListener {
        private final CountDownLatch latch;
        private final TaskKind kind;
        private final List<TaskState> values;

        private CountDownListener(TaskKind kind) {
            this(kind, 1);
        }

        private CountDownListener(TaskKind kind, int countDowns) {
            this.latch = new CountDownLatch(countDowns);
            this.values = new ArrayList<>(countDowns);
            this.kind = kind;
        }

        @Override
        public void onUpdate(Context context, TaskState state) {
            values.add(state);
            assertEquals(kind, state.task().kind());
            if (state.isFinished()) {
                latch.countDown();
            }
        }

        public void await() throws InterruptedException {
            assertTrue(latch.await(1, SECONDS));
        }

        public List<TaskState> getValues() {
            return values;
        }

        @Override
        public void onNotFound(Context context, TaskNotFound notFound) {
        }

    }

}
