package l.files.fs;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

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

        this.oldPath = Path.of(oldPath);
        this.oldPrefix = Path.of(oldPrefix);
        this.newPrefix = Path.of(newPrefix);
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
