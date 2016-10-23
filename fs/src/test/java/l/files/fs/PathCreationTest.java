package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
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
    public void path_is_as_expected() throws Exception {
        String actual = Path.fromString(sourcePathString).toString();
        assertEquals(expectedPath, actual);
    }

    @Test
    public void can_recreate_from_byte_array() throws Exception {
        byte[] bytes = Path.fromString(sourcePathString).toByteArray();
        String actual = Path.fromByteArray(bytes).toString();
        assertEquals(expectedPath, actual);
    }

    @Test
    public void can_recreate_from_string() throws Exception {
        String string = Path.fromString(sourcePathString).toString();
        String actual = Path.fromString(string).toString();
        assertEquals(expectedPath, actual);
    }

    @Test
    public void can_recreate_from_file() throws Exception {
        String actual = Path.fromFile(new File(sourcePathString)).toString();
        assertEquals(expectedPath, actual);
    }

    @Test
    public void paths_are_equivalent_when_recreated() throws Exception {
        Path p1 = Path.fromString(sourcePathString);
        Path p2 = Path.fromString(p1.toString());
        Path p3 = Path.fromByteArray(p2.toByteArray());
        Path p4 = Path.fromFile(new File(sourcePathString));
        assertEquals(
                singleton(expectedPath).toString(),
                new HashSet<>(asList(p1, p2, p3, p4)).toString());
    }

}
