package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathHierarchyTest {

    private final Path path;
    private final List<String> expectedHierarchy;

    public PathHierarchyTest(String path, List<String> expectedHierarchy) {
        this.path = Path.fromString(path);
        this.expectedHierarchy = expectedHierarchy;
    }

    @Parameters(name = "\"{0}\".hierarchy() == \"{1}\"")
    public static Collection<Object[]> paths() {
        return asList(new Object[][]{
                {"", singletonList("")},
                {"/", singletonList("/")},
                {"/a/b/c", asList("/", "/a", "/a/b", "/a/b/c")},
        });
    }

    @Test
    public void test() throws Exception {
        assertEquals(expectedHierarchy.toString(), path.hierarchy().toString());
    }
}
