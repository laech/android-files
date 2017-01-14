package l.files.operations;

import java.util.HashSet;
import java.util.Set;

import l.files.fs.Path;
import l.files.fs.local.LocalFileSystem;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

public final class CountTest extends PathBaseTest {

    public CountTest() {
        super(LocalFileSystem.INSTANCE);
    }

    public void test_count() throws Exception {
        fs.createFiles(dir1().concat("1/a.txt"));
        fs.createFiles(dir1().concat("3/4/c.txt"));

        Set<Path> expected = new HashSet<>(asList(
                dir1(),
                dir1().concat("1"),
                dir1().concat("1/a.txt"),
                dir1().concat("3"),
                dir1().concat("3/4"),
                dir1().concat("3/4/c.txt")
        ));

        Count counter = new Count(singletonMap(dir1(), fs));
        counter.execute();

        assertEquals(expected.size(), counter.getCount());
    }
}
