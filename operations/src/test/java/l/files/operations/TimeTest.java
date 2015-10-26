package l.files.operations;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class TimeTest {

    @Test
    public void create() throws Exception {
        Time time = Time.create(1, 2);
        assertEquals(1, time.time());
        assertEquals(2, time.tick());
    }

    @Test
    public void create_from_clock() throws Exception {
        Clock clock = new Clock() {

            @Override
            public long time() {
                return 1;
            }

            @Override
            public long tick() {
                return 2;
            }

        };
        Time time = Time.from(clock);
        assertEquals(1, time.time());
        assertEquals(2, time.tick());
    }

}
