package l.files.fs.local;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.Path;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathInequalityTest {

    private final Path path1;
    private final Path path2;

    public PathInequalityTest(String path1, String path2) {
        this.path1 = LocalPath.fromString(path1);
        this.path2 = LocalPath.fromString(path2);
    }

    @Parameters(name = "!\"{0}\".equals(\"{1}\")")
    public static Collection<Object[]> paths() {
        return asList(new Object[][]{
                {"", " "},
                {"a", "b"},
                {"a", "A"},
                {"/a", "a"},
                {"a", "/a"},
                {"a/b", "/a/b"},
                {"", "."},
                {".", ".."},
                {"./.", "."},
        });
    }

    @Test
    public void test() throws Exception {
        Set<Path> paths = new HashSet<>(asList(path1, path2));
        assertEquals(2, paths.size());
    }

}