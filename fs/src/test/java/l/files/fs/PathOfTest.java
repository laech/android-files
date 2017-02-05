package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathOfTest {

    private final String sourcePathString;
    private final String expectedPath;

    public PathOfTest(String sourcePathString, String expectedPath) {
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

    private void assertAbsoluteness(Path path) {
        assertEquals(expectedPath.startsWith("/"), path instanceof AbsolutePath);
    }

    @Test
    public void path_is_as_expected() throws Exception {
        Path actual = Path.of(sourcePathString);
        assertEquals(expectedPath, actual.toString());
        assertAbsoluteness(actual);
    }

    @Test
    public void can_recreate_from_byte_array() throws Exception {
        byte[] bytes = Path.of(sourcePathString).toByteArray();
        String actual = Path.of(bytes).toString();
        assertEquals(expectedPath, actual);
    }

    @Test
    public void can_recreate_from_string() throws Exception {
        String string = Path.of(sourcePathString).toString();
        String actual = Path.of(string).toString();
        assertEquals(expectedPath, actual);
    }

    @Test
    public void paths_are_equivalent_when_recreated() throws Exception {
        Path p1 = Path.of(sourcePathString);
        Path p2 = Path.of(p1.toString());
        Path p3 = Path.of(p2.toByteArray());
        Path p4 = Path.of(new File(sourcePathString));
        assertEquals(
                toSet(expectedPath).toString(),
                toSet(p1, p2, p3, p4).toString());
    }

    @Test
    public void can_convert_to_absolute_path() throws Exception {
        String expected = new File(expectedPath).getAbsolutePath();
        Path actual = Path.of(sourcePathString).toAbsolutePath();
        assertEquals(expected, actual.toString());
    }

    @SafeVarargs
    private static <T> Set<T> toSet(T... items) {
        return new HashSet<>(asList(items));
    }

}
