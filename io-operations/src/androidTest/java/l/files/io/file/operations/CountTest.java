package l.files.io.file.operations;

import java.io.File;
import java.util.Set;

import l.files.common.testing.FileBaseTest;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class CountTest extends FileBaseTest {

    public void testCount() throws Exception {
        tmp().createFile("1/a.txt");
        tmp().createFile("3/4/c.txt");

        Set<File> expected = newHashSet(
                tmp().get(),
                tmp().get("1"),
                tmp().get("1/a.txt"),
                tmp().get("3"),
                tmp().get("3/4"),
                tmp().get("3/4/c.txt"));

        Count counter = new Count(asList(tmp().get().getPath()));
        counter.call();

        assertThat(counter.getCount(), is(expected.size()));
    }
}
