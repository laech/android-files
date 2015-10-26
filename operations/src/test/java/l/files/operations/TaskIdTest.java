package l.files.operations;

import org.junit.Test;

import static l.files.operations.TaskKind.COPY;
import static org.junit.Assert.assertEquals;

public final class TaskIdTest {

    @Test
    public void create() throws Exception {
        TaskId task = TaskId.create(101, COPY);
        assertEquals(101, task.id());
        assertEquals(COPY, task.kind());
    }

}
