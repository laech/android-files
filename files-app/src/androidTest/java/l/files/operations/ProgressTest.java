package l.files.operations;

import junit.framework.TestCase;

public final class ProgressTest extends TestCase {

  public void testTotal() throws Exception {
    assertEquals(1, Progress.create(1, 0).total());
  }

  public void testProcessed() throws Exception {
    assertEquals(5, Progress.create(10, 5).processed());
  }

  public void testNone() throws Exception {
    assertEquals(0, Progress.none().total());
    assertEquals(0, Progress.none().processed());
    assertEquals(1F, Progress.none().processedPercentage());
  }

  public void testCreate() throws Exception {
    assertEquals(2, Progress.create(2, 1).total());
    assertEquals(1, Progress.create(2, 1).processed());
    assertEquals(2, Progress.create(2, 2).total());
    assertEquals(2, Progress.create(2, 2).processed());
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
    assertEquals(2, Progress.normalize(1, 2).total());
    assertEquals(2, Progress.normalize(1, 2).processed());
  }

  public void testProcessedPercentage() throws Exception {
    assertEquals(0.5F, Progress.create(2, 1).processedPercentage());
  }

  public void testLeft() throws Exception {
    assertEquals(8, Progress.create(10, 2).left());
  }

  public void testDone() {
    assertTrue(Progress.create(123, 123).isDone());
    assertFalse(Progress.create(2, 1).isDone());
  }
}
