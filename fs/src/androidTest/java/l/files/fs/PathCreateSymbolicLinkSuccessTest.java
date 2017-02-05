package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathCreateSymbolicLinkSuccessTest extends PathBaseTest {

    private final String subPathLink;
    private final String target;

    public PathCreateSymbolicLinkSuccessTest(
            String subPathLink,
            String target
    ) {
        this.subPathLink = subPathLink;
        this.target = target;
    }

    @Parameters(name = "\"{0}\" -> \"{1}\"")
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {"a", "."},
                {"a", ".."},
                {"a", "../hello"},
                {"a", "/"},
                {"a", "/abc"},
                {"a", "a"},
                {" ", "a"},
                {"\n", "\t"},
                {"hello", "world"},
                {"你好", "吗？"},
        });
    }

    @Test
    public void create_success() throws Exception {
        Path link = dir1().concat(subPathLink);
        link.createSymbolicLink(Path.of(this.target));
        assertEquals(target, link.readSymbolicLink().toString());
    }

}
