package l.files.operations;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ProgressTest {

    @Test
    public void total() throws Exception {
        assertEquals(1, Progress.create(1, 0).total());
    }

    @Test
    public void processed() throws Exception {
        assertEquals(5, Progress.create(10, 5).processed());
    }

    @Test
    public void none() throws Exception {
        assertEquals(0, Progress.NONE.total());
        assertEquals(0, Progress.NONE.processed());
        assertThat(1F, is(Progress.NONE.getProcessedPercentage()));
    }

    @Test
    public void create() throws Exception {
        assertEquals(2, Progress.create(2, 1).total());
        assertEquals(1, Progress.create(2, 1).processed());
        assertEquals(2, Progress.create(2, 2).total());
        assertEquals(2, Progress.create(2, 2).processed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createThrowsExceptionIfProcessedIsGreaterThanTotal() {
        Progress.create(1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createThrowsExceptionIfProcessedIsNegative() {
        Progress.create(1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createThrowsExceptionIfTotalIsNegative() {
        Progress.create(-1, -2);
    }

    @Test
    public void normalize() throws Exception {
        assertEquals(2, Progress.normalize(1, 2).total());
        assertEquals(2, Progress.normalize(1, 2).processed());
    }

    @Test
    public void processedPercentage() throws Exception {
        assertThat(0.5F, is(Progress.create(2, 1).getProcessedPercentage()));
    }

    @Test
    public void left() throws Exception {
        assertEquals(8, Progress.create(10, 2).getLeft());
    }

    @Test
    public void done() {
        assertTrue(Progress.create(123, 123).isDone());
        assertFalse(Progress.create(2, 1).isDone());
    }

}
