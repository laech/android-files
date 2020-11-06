package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Paths;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathRebaseTest {

    private final Path oldPath;
    private final Path oldPrefix;
    private final Path newPrefix;
    private final String newPath;

    public PathRebaseTest(
        String oldPath,
        String oldPrefix,
        String newPrefix,
        String newPath
    ) {

        this.oldPath = Path.of(oldPath);
        this.oldPrefix = Path.of(oldPrefix);
        this.newPrefix = Path.of(newPrefix);
        this.newPath = newPath;
    }

    @Parameters(name = "\"{0}\".rebase(\"{1}\", \"{2}\") == {3}")
    public static Collection<Object[]> paths() {
        return asList(new Object[][]{
            {".", ".", ".", "."},
            {".", ".", "..", ".."},
            {".", ".", "/", "/"},
            {".", ".", "a", "a"},
            {"", "", "", ""},
            {"", "", "a", "a"},
            {"", "", "/", "/"},
            {"", "", ".", "."},
            {"", "", "..", ".."},
            {"a", "a", "", ""},
            {"a", "a", "/", "/"},
            {"a", "a", ".", "."},
            {"/", "/", "a", "a"},
            {"/a", "/", "b", "b/a"},
            {"/a", "/a", "b", "b"},
            {"a/b", "a", "/abc", "/abc/b"},
            {"a/b", "a/b", "/abc", "/abc"},
            {"a/b", "a///b", "/abc", "/abc"},
            {"a/b", "a/b/", "/abc", "/abc"},
            {"a/b", "a/b///", "/abc", "/abc"},
            {"a/b", "a/", "c", "c/b"},
            {"a/b", "a///", "c", "c/b"},
            {"/a", "/a", "", ""},
            {"/a/hello world", "/a/hello world", "c", "c"},
            {"/a/你好", "/a/", "hello world", "hello world/你好"},
            {"/a/✌", "/a/✌", "abc", "abc"},
            {"\\", "\\", "a", "a"},
        });
    }

    @Test
    public void test() {
        assertEquals(
            Paths.get(newPath),
            oldPath.rebase(oldPrefix, newPrefix).toJavaPath()
        );
    }

}
