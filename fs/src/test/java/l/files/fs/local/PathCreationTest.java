package l.files.fs.local;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.Path;

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

    private void assertAbsoluteness(Path path) {
        assertEquals(expectedPath.startsWith("/"), path instanceof AbsolutePath);
    }

    @Test
    public void path_is_as_expected() throws Exception {
        Path actual = LocalPath.create(sourcePathString);
        assertEquals(expectedPath, actual.toString());
        assertAbsoluteness(actual);
    }

    @Test
    public void can_recreate_from_byte_array() throws Exception {
        byte[] bytes = LocalPath.create(sourcePathString).toByteArray();
        String actual = LocalPath.create(bytes).toString();
        assertEquals(expectedPath, actual);
    }

    @Test
    public void can_recreate_from_string() throws Exception {
        String string = LocalPath.create(sourcePathString).toString();
        String actual = LocalPath.create(string).toString();
        assertEquals(expectedPath, actual);
    }

    @Test
    public void paths_are_equivalent_when_recreated() throws Exception {
        Path p1 = LocalPath.create(sourcePathString);
        Path p2 = LocalPath.create(p1.toString());
        Path p3 = LocalPath.create(p2.toByteArray());
        Path p4 = LocalPath.create(new File(sourcePathString));
        assertEquals(
                toSet(expectedPath).toString(),
                toSet(p1, p2, p3, p4).toString());
    }

    @Test
    public void can_convert_to_absolute_path() throws Exception {
        String expected = new File(expectedPath).getAbsolutePath();
        Path actual = LocalPath.create(sourcePathString).toAbsolutePath();
        assertEquals(expected, actual.toString());
    }

    @SafeVarargs
    private static <T> Set<T> toSet(T... items) {
        return new HashSet<>(asList(items));
    }

}
