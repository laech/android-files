package l.files.operations;

import junit.framework.TestCase;

import static l.files.operations.TaskKind.COPY;

public final class TaskIdTest extends TestCase {

  public void testCreate() throws Exception {
    TaskId task = TaskId.create(101, COPY);
    assertEquals(101, task.getId());
    assertEquals(COPY, task.getKind());
  }

}
