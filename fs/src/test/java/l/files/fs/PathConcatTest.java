package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Paths;
import java.util.Collection;

import static java.util.Arrays.asList;
import static l.files.base.io.Charsets.UTF_8;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathConcatTest {

    private final String basePath;
    private final String additionalPath;
    private final String expectedPath;

    public PathConcatTest(
        String basePath,
        String additionalPath,
        String expectedPath
    ) {
        this.basePath = basePath;
        this.additionalPath = additionalPath;
        this.expectedPath = expectedPath;
    }

    @Parameters(name = "\"{0}\".concat(\"{1}\") -> \"{2}\"")
    public static Collection<Object[]> paths() {
        return asList(new Object[][]{
            {"", "", ""},
            {".", ".", "./."},
            {"./.", "./.", "./././."},
            {"..", "..", "../.."},
            {"../.", "../.", ".././../."},
            {"../..", "../..", "../../../.."},
            {"a", "b", "a/b"},
            {"a", "b/", "a/b"},
            {"a", "b//", "a/b"},
            {"a", "b///", "a/b"},
            {"a", "/b", "a/b"},
            {"a", "/b/", "a/b"},
            {"a", "/b///", "a/b"},
            {"a/", "b", "a/b"},
            {"a/", "b/", "a/b"},
            {"a/", "b//", "a/b"},
            {"a/", "b///", "a/b"},
            {"a/", "/b", "a/b"},
            {"a/", "/b/", "a/b"},
            {"a/", "/b///", "a/b"},
            {"a//", "b", "a/b"},
            {"a//", "b/", "a/b"},
            {"a//", "b//", "a/b"},
            {"a//", "b///", "a/b"},
            {"a//", "/b", "a/b"},
            {"a//", "/b/", "a/b"},
            {"a//", "/b///", "a/b"},
            {"/a//", "b", "/a/b"},
            {"/a//", "b/", "/a/b"},
            {"/a//", "b//", "/a/b"},
            {"/a//", "b///", "/a/b"},
            {"/a//", "/b", "/a/b"},
            {"/a//", "/b/", "/a/b"},
            {"/a//", "/b///", "/a/b"},
            {"a", "a", "a/a"},
            {"/a", "/a", "/a/a"},
            {"/a", "hello world", "/a/hello world"},
            {"/a/✌", "你 好️", "/a/✌/你 好️"},
        });
    }

    @Test
    public void concatenated_path_is_as_expected() {
        Path base = Path.of(basePath);
        assertEquals(
            Paths.get(expectedPath),
            base.concat(additionalPath).toJavaPath()
        );
        assertEquals(
            Paths.get(expectedPath),
            base.concat(additionalPath.getBytes(UTF_8)).toJavaPath()
        );
        assertEquals(
            Paths.get(expectedPath),
            base.concat(Path.of(additionalPath)).toJavaPath()
        );
    }
}
