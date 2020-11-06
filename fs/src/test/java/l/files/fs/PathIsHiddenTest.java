package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathIsHiddenTest {

    private final Path path;
    private final boolean hidden;

    public PathIsHiddenTest(String path, boolean hidden) {
        this.path = Path.of(path);
        this.hidden = hidden;
    }

    @Parameters(name = "\"{0}\".isHidden() == {1}")
    public static Collection<Object[]> paths() {
        return asList(new Object[][]{
            {"", false},
            {".", true},
            {"./", true},
            {"..", true},
            {"../", true},
            {"a", false},
            {".a", true},
            {".a/", true},
            {".a//", true},
            {".b", true},
            {".b.c", true},
            {".b.c.", true},
            {".a/b", false},
            {".a/.b", true},
            {"/a", false},
            {"/a/b", false},
            {"/a/.b", true},
            {"/.a", true},
            {"/.a/b", false},
            {"/", false},
        });
    }

    @Test
    public void test() {
        assertEquals(hidden, path.isHidden());
    }
}
