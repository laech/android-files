package l.files.operations;

import android.content.ComponentName;
import android.content.Intent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import de.greenrobot.event.EventBus;
import l.files.fs.Resource;
import l.files.fs.local.ResourceBaseTest;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.operations.OperationService.ACTION_CANCEL;
import static l.files.operations.OperationService.EXTRA_TASK_ID;
import static l.files.operations.OperationService.copy;
import static l.files.operations.OperationService.delete;
import static l.files.operations.OperationService.move;
import static l.files.operations.OperationService.newCancelIntent;
import static l.files.operations.TaskKind.COPY;
import static l.files.operations.TaskKind.DELETE;
import static l.files.operations.TaskKind.MOVE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class OperationServiceTest extends ResourceBaseTest {

    public void testCancelIntent() throws Exception {
        Intent intent = newCancelIntent(getContext(), 101);
        assertEquals(ACTION_CANCEL, intent.getAction());
        assertEquals(101, intent.getIntExtra(EXTRA_TASK_ID, -1));
        assertEquals(new ComponentName(getContext(), OperationService.class),
                intent.getComponent());
    }

    public void testCancelTaskNotFound() throws Exception {
        OperationService service = new OperationService();
        service.onCreate();
        service.bus = mock(EventBus.class);
        service.onStartCommand(newCancelIntent(getContext(), 1011), 0, 0);
        verify(service.bus).post(TaskNotFound.create(1011));
    }

    public void testMovesFile() throws Exception {
        Resource src = dir1().resolve("a").createFile();
        Resource dst = dir1().resolve("dst").createDirectory();
        CountDownListener listener = register(new CountDownListener(MOVE));
        try {

            move(getContext(), singleton(src), dst);
            listener.await();
            assertFalse(src.exists(NOFOLLOW));
            assertTrue(dst.resolve(src.getName()).exists(NOFOLLOW));

        } finally {
            unregister(listener);
        }
    }

    public void testCopiesFile() throws Exception {
        Resource src = dir1().resolve("a").createFile();
        Resource dst = dir1().resolve("dst").createDirectory();
        CountDownListener listener = register(new CountDownListener(COPY));
        try {

            copy(getContext(), singleton(src), dst);
            listener.await();
            assertTrue(src.exists(NOFOLLOW));
            assertTrue(dst.resolve(src.getName()).exists(NOFOLLOW));

        } finally {
            unregister(listener);
        }
    }

    public void testDeletesFiles() throws Exception {
        Resource a = dir1().resolve("a").createFiles();
        Resource b = dir1().resolve("b/c").createFiles();
        CountDownListener listener = register(new CountDownListener(DELETE));
        try {

            delete(getContext(), new HashSet<>(asList(a, b)));
            listener.await();
            assertFalse(a.exists(NOFOLLOW));
            assertFalse(b.exists(NOFOLLOW));

        } finally {
            unregister(listener);
        }
    }

    public void testTaskIdIsUnique() throws Exception {
        CountDownListener listener = register(new CountDownListener(DELETE, 2));
        try {

            delete(getContext(), singleton(dir1().resolve("a").createFile()));
            delete(getContext(), singleton(dir1().resolve("2").createFile()));
            listener.await();
            assertTrue(listener.getValues().size() > 1);
            assertEquals(2, getTaskIds(listener.getValues()).size());

        } finally {
            unregister(listener);
        }
    }

    private Set<Integer> getTaskIds(List<? extends TaskState> values) {
        Set<Integer> result = new HashSet<>();
        for (TaskState value : values) {
            result.add(value.getTask().getId());
        }
        return result;
    }

    public void testTaskStartTimeIsCorrect() throws Exception {
        Resource file1 = dir1().resolve("a").createFile();
        Resource file2 = dir1().resolve("b").createFile();
        CountDownListener listener = register(new CountDownListener(DELETE));
        try {

            long start = currentTimeMillis();
            {
                delete(getContext(), new HashSet<>(asList(file1, file2)));
                listener.await();
            }
            long end = currentTimeMillis();

            Set<Long> times = getTaskStartTimes(listener.getValues());
            assertTrue(times.iterator().next() >= start);
            assertTrue(times.iterator().next() <= end);

        } finally {
            unregister(listener);
        }
    }

    private Set<Long> getTaskStartTimes(List<? extends TaskState> values) {
        Set<Long> result = new HashSet<>();
        for (TaskState value : values) {
            result.add(value.getTime().getTime());
        }
        return result;
    }

    private <T> T register(T listener) {
        Events.get().register(listener);
        return listener;
    }

    private void unregister(Object listener) {
        Events.get().unregister(listener);
    }

    private static class CountDownListener {
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

        public void onEvent(TaskState state) {
            values.add(state);
            assertEquals(kind, state.getTask().getKind());
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
    }
}
