package l.files.fs.local;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import l.files.fs.Path;

import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public final class PathRebaseInvalidTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final Path oldPath;
    private final Path oldPrefix;
    private final Path newPrefix;

    public PathRebaseInvalidTest(
            String oldPath,
            String oldPrefix,
            String newPrefix) {

        this.oldPath = LocalPath.fromString(oldPath);
        this.oldPrefix = LocalPath.fromString(oldPrefix);
        this.newPrefix = LocalPath.fromString(newPrefix);
    }

    @Parameters(name = "\"{0}\".rebase(\"{1}\", \"{2}\") throws IllegalArgumentException")
    public static Collection<Object[]> paths() {
        return asList(new Object[][]{
                {"", " ", "a"},
                {"", ".", "a"},
                {"", "..", "a"},
                {"/", "..", "a"},
                {"/", ".", "a"},
                {".", "/", "a"},
                {"..", "/", "a"},
                {"a", "b", "c"},
                {"a/b", "b", "c"},
                {"a/b", "a/b/c", "c"},
                {"/a/b", "/b", "c"},
        });
    }

    @Test
    public void test() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        oldPath.rebase(oldPrefix, newPrefix);
    }

}
