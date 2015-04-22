package l.files.operations;

import java.util.HashSet;
import java.util.Set;

import l.files.fs.Resource;
import l.files.fs.local.ResourceBaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public final class CountTest extends ResourceBaseTest {

    public void testCount() throws Exception {
        dir1().resolve("1/a.txt").createFile();
        dir1().resolve("3/4/c.txt").createFile();

        Set<Resource> expected = new HashSet<Resource>(asList(
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
