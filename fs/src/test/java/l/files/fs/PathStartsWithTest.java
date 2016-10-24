package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathStartsWithTest {

    private final Path path;
    private final Path startsWith;
    private final boolean result;

    public PathStartsWithTest(String path, String startsWith, boolean result) {
        this.path = Path.fromString(path);
        this.startsWith = Path.fromString(startsWith);
        this.result = result;
    }

    @Parameters(name = "\"{0}\".startsWith(\"{1}\") == {2}")
    public static Collection<Object[]> paths() {
        return asList(new Object[][]{
                {" ", " ", true},
                {"\t", "\t", true},
                {"\n", "\n", true},
                {".", ".", true},
                {".", "..", false},
                {".", "", true},
                {".", "/", false},
                {".", "a", false},
                {"..", "..", true},
                {"..", ".", false},
                {"..", "/", false},
                {"..", "a", false},
                {"..", "/a", false},
                {"", "", true},
                {"", "a", false},
                {"", "/", false},
                {"", ".", false},
                {"", "..", false},
                {"a", "a", true},
                {"a", "", true},
                {"a", "/", false},
                {"a", ".", false},
                {"a", "..", false},
                {"/", "/", true},
                {"/", "", false},
                {"//", "/", true},
                {"/a", "", false},
                {"/a", "/", true},
                {"/a", "/a", true},
                {"a/b", "a", true},
                {"a/b", "a/", true},
                {"a/b", "a///", true},
                {"a/b", "/a", false},
                {"a/b", "///a", false},
                {"a/b", "b", false},
                {"a/b", "b/", false},
                {"a/b", "/b/", false},
                {"a/b", "/a/b", false},
                {"a/b", "/a/b/", false},
                {"//a", "/a", true},
                {"/a", "/a", true},
                {"/a/hello world", "/a/hello world", true},
                {"/a/你好", "/a/你好", true},
                {"/a/✌️", "/a/✌️", true},
                {"/a/\n✌️", "/a/\n✌️", true},
                {"\\", "\\", true},
        });
    }

    @Test
    public void test() throws Exception {
        assertEquals(result, path.startsWith(startsWith));
    }

}
