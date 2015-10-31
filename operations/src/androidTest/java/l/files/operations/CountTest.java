package l.files.operations;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import l.files.fs.File;
import l.files.testing.fs.FileBaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public final class CountTest extends FileBaseTest {

    @Test
    public void count() throws Exception {
        dir1().resolve("1/a.txt").createFiles();
        dir1().resolve("3/4/c.txt").createFiles();

        Set<File> expected = new HashSet<>(asList(
                dir1(),
                dir1().resolve("1"),
                dir1().resolve("1/a.txt"),
                dir1().resolve("3"),
                dir1().resolve("3/4"),
                dir1().resolve("3/4/c.txt")
        ));

        Count counter = new Count(singletonList(dir1()));
        counter.execute();

        assertEquals(expected.size(), counter.getCount());
    }
}
