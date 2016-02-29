package l.files.operations;

import java.util.HashSet;
import java.util.Set;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static l.files.fs.Files.createFiles;

public final class CountTest extends PathBaseTest {

    public void test_count() throws Exception {
        createFiles(dir1().resolve("1/a.txt"));
        createFiles(dir1().resolve("3/4/c.txt"));

        Set<Path> expected = new HashSet<Path>(asList(
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
