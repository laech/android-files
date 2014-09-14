package l.files.operations;

import junit.framework.TestCase;

import static l.files.operations.testing.ProgressSubject.assertThat;

public final class ProgressTest extends TestCase {

  public void testTotal() throws Exception {
    assertThat(Progress.create(1, 0)).total(1);
  }

  public void testProcessed() throws Exception {
    assertThat(Progress.create(10, 5)).processed(5);
  }

  public void testNone() throws Exception {
    assertThat(Progress.none()).total(0).processed(0);
  }

  public void testCreate() throws Exception {
    assertThat(Progress.create(2, 1)).total(2).processed(1);
    assertThat(Progress.create(2, 2)).total(2).processed(2);
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
    assertThat(Progress.normalize(1, 2)).total(2).processed(2);
  }

  public void testProcessedPercentage() throws Exception {
    assertThat(Progress.create(2, 1)).processedPercentage(0.5F);
  }

  public void testLeft() throws Exception {
    assertThat(Progress.create(10, 2)).left(8);
  }

  public void testDone() {
    assertThat(Progress.create(123, 123)).isDone(true);
    assertThat(Progress.create(2, 1)).isDone(false);
  }
}
