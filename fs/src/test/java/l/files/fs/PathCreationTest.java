package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathCreationTest {

    private final String sourcePathString;
    private final String expectedPath;

    public PathCreationTest(String sourcePathString, String expectedPath) {
        this.sourcePathString = sourcePathString;
        this.expectedPath = expectedPath;
    }

    @Parameters(name = "\"{0}\"")
    public static Collection<Object[]> paths() {
        return asList(new Object[][]{

                {"", ""},
                {" ", " "},
                {"\t", "\t"},
                {"\n", "\n"},

                {"/", "/"},
                {"//", "/"},

                {".", "."},
                {"./.", "./."},
                {"..", ".."},
                {"../.", "../."},
                {"../..", "../.."},

                {"a", "a"},
                {"a/b", "a/b"},
                {"a//b", "a/b"},
                {"a//b/", "a/b"},
                {"a//b//", "a/b"},
                {"a/b/", "a/b"},

                {"//a", "/a"},
                {"/a", "/a"},
                {"/a/hello world", "/a/hello world"},
                {"/a/你好", "/a/你好"},
                {"/a/✌️", "/a/✌️"},
                {"/a/\n✌️", "/a/\n✌️"},

                {"\\", "\\"},
        });
    }

    @Test
    public void name_is_as_expected() throws Exception {
        String actual = Path.fromString(sourcePathString).toString();
        assertEquals(expectedPath, actual);
    }
}
