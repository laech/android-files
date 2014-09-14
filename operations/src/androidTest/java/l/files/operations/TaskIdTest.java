package l.files.operations;

import junit.framework.TestCase;

import static com.google.common.truth.Truth.ASSERT;
import static l.files.operations.TaskKind.COPY;

public final class TaskIdTest extends TestCase {

  public void testCreate() throws Exception {
    TaskId task = TaskId.create(101, COPY);
    ASSERT.that(task.id()).is(101);
    ASSERT.that(task.kind()).is(COPY);
  }

}
