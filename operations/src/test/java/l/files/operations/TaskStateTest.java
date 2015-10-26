package l.files.operations;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import l.files.fs.File;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static l.files.operations.TaskKind.COPY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public final class TaskStateTest {

    private Time time;
    private TaskId task;
    private Target target;
    private TaskState.Pending pending;

    @Before
    public void setUp() throws Exception {
        this.time = Time.create(10, 11);
        this.task = TaskId.create(1, COPY);
        this.target = Target.from(singleton(mock(File.class)), mock(File.class));
        this.pending = TaskState.pending(task, target, time);
    }

    @Test
    public void CreatePending() throws Exception {
        assertEquals(task, pending.task());
        assertEquals(time, pending.time());
        assertEquals(target, pending.target());
    }

    @Test
    public void PendingToRunning() throws Exception {
        Progress items = Progress.create(10101, 1);
        Progress bytes = Progress.create(10100, 0);
        Time time = Time.create(1010, 11);
        TaskState.Running state = pending.running(time, items, bytes);
        assertEquals(task, state.task());
        assertEquals(time, state.time());
        assertEquals(items, state.items());
        assertEquals(bytes, state.bytes());
        assertEquals(target, state.target());
    }

    @Test
    public void PendingToRunningWithoutProgress() throws Exception {
        Time time = Time.create(10101, 100);
        TaskState.Running state = pending.running(time);
        assertEquals(task, state.task());
        assertEquals(time, state.time());
        assertEquals(target, state.target());
        assertEquals(Progress.NONE, state.items());
        assertEquals(Progress.NONE, state.bytes());

    }

    @Test
    public void RunningToRunningDoesNotChangeTime() throws Exception {
        Time time = Time.create(102, 2);
        Progress items = Progress.create(4, 2);
        Progress bytes = Progress.create(9, 2);
        TaskState.Running state = pending.running(time).running(items, bytes);
        assertEquals(task, state.task());
        assertEquals(time, state.time());
        assertEquals(items, state.items());
        assertEquals(bytes, state.bytes());
        assertEquals(target, state.target());
    }

    @Test
    public void RunningToSuccess() throws Exception {
        Time successTime = Time.create(2, 2);
        TaskState state = pending.running(time).success(successTime);
        assertEquals(task, state.task());
        assertEquals(successTime, state.time());
        assertEquals(target, state.target());
    }

    @Test
    public void RunningToFailed() throws Exception {
        Time failureTime = Time.create(20, 2);
        List<Failure> failures = singletonList(Failure.create(
                mock(File.class), new IOException("ok")
        ));
        TaskState.Failed state = pending.running(time).failed(failureTime, failures);
        assertEquals(task, state.task());
        assertEquals(failureTime, state.time());
        assertEquals(target, state.target());
        assertEquals(failures, state.failures());
    }

}
