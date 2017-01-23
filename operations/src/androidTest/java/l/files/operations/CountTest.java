package l.files.operations;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

public final class CountTest extends PathBaseTest {

    @Test
    public void count() throws Exception {
        dir1().concat("1/a.txt").createFiles();
        dir1().concat("3/4/c.txt").createFiles();

        Set<Path> expected = new HashSet<Path>(asList(
                dir1(),
                dir1().concat("1"),
                dir1().concat("1/a.txt"),
                dir1().concat("3"),
                dir1().concat("3/4"),
                dir1().concat("3/4/c.txt")
        ));

        Count counter = new Count(singleton(dir1()));
        counter.execute();

        assertEquals(expected.size(), counter.getCount());
    }
}
