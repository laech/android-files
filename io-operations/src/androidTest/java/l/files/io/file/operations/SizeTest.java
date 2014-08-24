package l.files.io.file.operations;

import java.io.File;

import l.files.common.testing.FileBaseTest;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class SizeTest extends FileBaseTest {

    public void testSize() throws Exception {
        File a = tmp().createDir("a");
        File b = tmp().createFile("a/b");
        File c = tmp().createFile("c");
        File d = tmp().createDir("d");

        Size size = new Size(asList(a.getPath(), b.getPath(), c.getPath(), d.getPath()));
        size.execute();

        assertThat(size.getSize(), is(a.length() + b.length() + c.length() + d.length()));
    }
}