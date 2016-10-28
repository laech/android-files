package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathParentTest {

    private final Path path;
    private final String expectedParent;

    public PathParentTest(String path, String expectedParent) {
        this.path = Path.fromString(path);
        this.expectedParent = expectedParent;
    }

    @Parameters(name = "\"{0}\".parent() == \"{1}\"")
    public static Collection<Object[]> paths() {
        return asList(new Object[][]{
                {"", null},
                {" ", ""},
                {"\t", ""},
                {"\n", ""},
                {"/", null},
                {"//", null},
                {".", ""},
                {"./.", "."},
                {"..", ""},
                {"../.", ".."},
                {"../..", ".."},
                {"a", ""},
                {"a/b", "a"},
                {"a//b", "a"},
                {"a//b/", "a"},
                {"a//b//", "a"},
                {"a/b/", "a"},
                {"//a", "/"},
                {"/a", "/"},
                {"/a/hello world", "/a"},
                {"/a/你好", "/a"},
                {"/a/✌", "/a"},
                {"/a/\n✌", "/a"},
                {"\\", ""},
        });
    }

    @Test
    public void test() throws Exception {
        String expected = String.valueOf(expectedParent);
        String actual = String.valueOf(path.parent());
        assertEquals(expected, actual);
    }
}
