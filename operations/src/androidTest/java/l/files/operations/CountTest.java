package l.files.operations;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.Path;
import l.files.fs.local.LocalPath;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

public final class CountTest extends PathBaseTest {

    @Override
    protected Path create(File file) {
        return LocalPath.fromFile(file);
    }

    public void test_count() throws Exception {
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
