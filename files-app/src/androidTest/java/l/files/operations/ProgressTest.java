package l.files.operations;

import junit.framework.TestCase;

public final class ProgressTest extends TestCase {

  public void testTotal() throws Exception {
    assertEquals(1, Progress.create(1, 0).getTotal());
  }

  public void testProcessed() throws Exception {
    assertEquals(5, Progress.create(10, 5).getProcessed());
  }

  public void testNone() throws Exception {
    assertEquals(0, Progress.NONE.getTotal());
    assertEquals(0, Progress.NONE.getProcessed());
    assertEquals(1F, Progress.NONE.getProcessedPercentage());
  }

  public void testCreate() throws Exception {
    assertEquals(2, Progress.create(2, 1).getTotal());
    assertEquals(1, Progress.create(2, 1).getProcessed());
    assertEquals(2, Progress.create(2, 2).getTotal());
    assertEquals(2, Progress.create(2, 2).getProcessed());
  }

  public void testCreateThrowsExceptionIfProcessedIsGreaterThanTotal() {
    try {
      Progress.create(1, 2);
      fail();
    } catch (IllegalArgumentException e) {
      // Pass
    }
  }

  public void testCreateThrowsExceptionIfProcessedIsNegative() {
    try {
      Progress.create(1, -1);
      fail();
    } catch (IllegalArgumentException e) {
      // Pass
    }
  }

  public void testCreateThrowsExceptionIfTotalIsNegative() {
    try {
      Progress.create(-1, -2);
      fail();
    } catch (IllegalArgumentException e) {
      // Pass
    }
  }

  public void testNormalize() throws Exception {
    assertEquals(2, Progress.normalize(1, 2).getTotal());
    assertEquals(2, Progress.normalize(1, 2).getProcessed());
  }

  public void testProcessedPercentage() throws Exception {
    assertEquals(0.5F, Progress.create(2, 1).getProcessedPercentage());
  }

  public void testLeft() throws Exception {
    assertEquals(8, Progress.create(10, 2).getLeft());
  }

  public void testDone() {
    assertTrue(Progress.create(123, 123).isDone());
    assertFalse(Progress.create(2, 1).isDone());
  }
}
