package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.Collection;

import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Stat.lstat;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public abstract class PathCreateSuccessTest extends PathBaseTest {

    private final String subPath;

    PathCreateSuccessTest(String subPath) {
        this.subPath = subPath;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {"a"},
                {" "},
                {"\n"},
                {"hello world"},
                {"你好"},
        });
    }

    @Test
    public void create_success() throws Exception {
        creation().create(dir1().concat(subPath));
    }

    @Test
    public void create_with_correct_default_permissions() throws Exception {

        Path actual = dir1().concat(subPath);
        File expected = new File(dir1().toString(), subPath + "_expected");
        creation().create(actual);
        creation().create(expected);

        Stat stat = lstat(expected.getPath().getBytes());
        assertEquals(expected.canRead(), actual.isReadable());
        assertEquals(expected.canWrite(), actual.isWritable());
        assertEquals(expected.canExecute(), actual.isExecutable());
        assertEquals(stat.permissions(), actual.stat(NOFOLLOW).permissions());
    }

    abstract PathCreation creation();

}
