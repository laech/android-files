package l.files.operations;

import com.google.common.base.Function;
import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import l.files.common.testing.FileBaseTest;
import l.files.operations.info.DeleteTaskInfo;
import l.files.operations.info.TaskInfo;

import static com.google.common.collect.Collections2.transform;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.operations.info.TaskInfo.TaskStatus.FINISHED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

public final class OperationServiceTest extends FileBaseTest {

    public void testDeletesFiles() throws Exception {
        File file1 = tmp().createFile("a");
        File file2 = tmp().createFile("b/c");

        final CountDownLatch latch = new CountDownLatch(1);
        Object listener = new Object() {
            @Subscribe
            public void on(DeleteTaskInfo value) {
                if (value.getTaskStatus() == FINISHED) {
                    assertEquals(value.getTotalItemCount(), value.getDeletedItemCount());
                    latch.countDown();
                }
            }
        };

        register(listener);
        try {

            OperationService.delete(getContext(), file1.getPath(), file2.getPath());
            assertThat(latch.await(1, SECONDS), is(true));
            assertThat(file1.exists(), is(false));
            assertThat(file2.exists(), is(false));

        } finally {
            unregister(listener);
        }
    }

    public void testTaskIdIsUnique() throws Exception {
        final List<TaskInfo> values = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(2);
        Object receiver = new Object() {
            @Subscribe
            public void on(DeleteTaskInfo value) {
                values.add(value);
                if (value.getTaskStatus() == FINISHED) {
                    latch.countDown();
                }
            }
        };

        register(receiver);
        try {

            OperationService.delete(getContext(), tmp().createFile("a").getPath());
            OperationService.delete(getContext(), tmp().createFile("2").getPath());
            assertThat(latch.await(1, SECONDS), is(true));
            assertThat(values.size(), greaterThan(1));
            assertThat(getTaskIds(values), hasSize(2));

        } finally {
            unregister(receiver);
        }
    }

    private Set<Integer> getTaskIds(List<? extends TaskInfo> values) {
        return new HashSet<>(transform(values,
                new Function<TaskInfo, Integer>() {
                    @Override
                    public Integer apply(TaskInfo task) {
                        return task.getTaskId();
                    }
                }
        ));
    }

    public void testTaskStartTimeIsCorrect() throws Exception {
        String path1 = tmp().createFile("a").getPath();
        String path2 = tmp().createFile("b").getPath();

        final List<TaskInfo> values = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Object listener = new Object() {
            @Subscribe
            public void on(DeleteTaskInfo value) {
                values.add(value);
                if (value.getTaskStatus() == FINISHED) {
                    latch.countDown();
                }
            }
        };

        register(listener);
        try {

            long start = currentTimeMillis();
            {
                OperationService.delete(getContext(), path1, path2);
                assertThat(latch.await(1, SECONDS), is(true));
            }
            long end = currentTimeMillis();

            Set<Long> times = getTaskStartTimes(values);
            assertThat(times.size(), is(1));
            assertThat(times.iterator().next(), greaterThanOrEqualTo(start));
            assertThat(times.iterator().next(), lessThanOrEqualTo(end));

        } finally {
            unregister(listener);
        }
    }

    private Set<Long> getTaskStartTimes(List<TaskInfo> values) {
        return new HashSet<>(transform(values,
                new Function<TaskInfo, Long>() {
                    @Override
                    public Long apply(TaskInfo value) {
                        return value.getTaskStartTime();
                    }
                }
        ));
    }

    private void register(Object listener) {
        Events.get().register(listener);
    }

    private void unregister(Object listener) {
        Events.get().unregister(listener);
    }
}
