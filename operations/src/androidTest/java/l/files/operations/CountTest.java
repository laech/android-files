package l.files.operations;

import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;
import org.junit.Test;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

public final class CountTest extends PathBaseTest {

    @Test
    public void count() throws Exception {
        Paths.createFiles(dir1().concat("1/a.txt"));
        Paths.createFiles(dir1().concat("3/4/c.txt"));

        Collection<?> expected = asList(
            dir1(),
            dir1().concat("1"),
            dir1().concat("1/a.txt"),
            dir1().concat("3"),
            dir1().concat("3/4"),
            dir1().concat("3/4/c.txt")
        );

        Count counter = new Count(singleton(dir1().toJavaPath()));
        counter.execute();

        assertEquals(expected.size(), counter.getCount());
    }
}
