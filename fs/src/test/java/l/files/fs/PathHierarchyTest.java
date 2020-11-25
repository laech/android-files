package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static l.files.fs.PathKt.hierarchy;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathHierarchyTest {

    private final java.nio.file.Path path;
    private final List<String> expectedHierarchy;

    public PathHierarchyTest(String path, List<String> expectedHierarchy) {
        this.path = Paths.get(path);
        this.expectedHierarchy = expectedHierarchy;
    }

    @Parameters(name = "\"{0}\".hierarchy() == \"{1}\"")
    public static Collection<Object[]> paths() {
        return asList(new Object[][]{
            {"", singletonList("")},
            {"/", singletonList("/")},
            {"/a/b/c", asList("/a/b/c", "/a/b", "/a", "/")},
        });
    }

    @Test
    public void test() {
        assertEquals(
            expectedHierarchy.stream().map(Paths::get).collect(toList()),
            hierarchy(path)
        );
    }
}
