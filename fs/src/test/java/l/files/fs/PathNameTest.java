package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import javax.annotation.Nullable;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathNameTest {

    private final Path path;

    @Nullable
    private final String expectedName;

    public PathNameTest(String path, @Nullable String expectedName) {
        this.path = Path.fromString(path);
        this.expectedName = expectedName;
    }

    @Parameters(name = "\"{0}\".name() == \"{1}\"")
    public static Collection<Object[]> paths() {
        return asList(new Object[][]{

                {"", null},
                {" ", " "},
                {"\t", "\t"},
                {"\n", "\n"},

                {"/", null},
                {"//", null},

                {".", "."},
                {"./.", "."},
                {"..", ".."},
                {"../.", "."},
                {"../..", ".."},

                {"a", "a"},
                {"a/b", "b"},
                {"a//b", "b"},
                {"a//b/", "b"},
                {"a//b//", "b"},
                {"a/b/", "b"},

                {"//a", "a"},
                {"/a", "a"},
                {"/a/hello world", "hello world"},
                {"/a/你好", "你好"},
                {"/a/✌️", "✌️"},
                {"/a/\n✌️", "\n✌️"},

                {"\\", "\\"},
        });
    }

    @Test
    public void test() throws Exception {
        Name actual = path.name();
        assertEquals(expectedName, actual == null ? null : actual.toString());
    }
}
