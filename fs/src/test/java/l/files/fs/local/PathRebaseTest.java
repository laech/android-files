package l.files.fs.local;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import l.files.fs.Path;

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
            String newPath) {

        this.oldPath = LocalPath.create(oldPath);
        this.oldPrefix = LocalPath.create(oldPrefix);
        this.newPrefix = LocalPath.create(newPrefix);
        this.newPath = newPath;
    }

    @Parameters(name = "\"{0}\".rebase(\"{1}\", \"{2}\") == {3}")
    public static Collection<Object[]> paths() {
        return asList(new Object[][]{
                {" ", " ", " ", " "},
                {"\t", "\t", "\t", "\t"},
                {"\n", "\n", "\n", "\n"},
                {".", ".", ".", "."},
                {".", ".", "..", ".."},
                {".", "", "a", "a/."},
                {".", ".", "/", "/"},
                {".", ".", "a", "a"},
                {"", "", "", ""},
                {"", "", "a", "a"},
                {"", "", "/", "/"},
                {"", "", ".", "."},
                {"", "", "..", ".."},
                {"a", "a", "", ""},
                {"a", "", "a", "a/a"},
                {"a", "a", "/", "/"},
                {"a", "a", ".", "."},
                {"a", "", "..", "../a"},
                {"/", "/", "a", "a"},
                {"//", "/", "a", "a"},
                {"/a", "/", "b", "b/a"},
                {"/a", "/a", "b", "b"},
                {"a/b", "a", "/abc", "/abc/b"},
                {"a/b", "a/b", "/abc", "/abc"},
                {"a/b", "a///b", "/abc", "/abc"},
                {"a/b", "a/b/", "/abc", "/abc"},
                {"a/b", "a/b///", "/abc", "/abc"},
                {"a/b", "a/", "c", "c/b"},
                {"a/b", "a///", "c", "c/b"},
                {"//a", "/a", "", ""},
                {"/a", "/a", "", ""},
                {"/a/hello world", "/a/hello world", "c", "c"},
                {"/a/你好", "/a/", "hello world", "hello world/你好"},
                {"/a/✌", "/a/✌", "abc", "abc"},
                {"/a/\n✌", "/a", "abc", "abc/\n✌"},
                {"\\", "\\", "a", "a"},
        });
    }

    @Test
    public void test() throws Exception {
        assertEquals(newPath, oldPath.rebase(oldPrefix, newPrefix).toString());
    }

}
