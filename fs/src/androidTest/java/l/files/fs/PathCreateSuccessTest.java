package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import l.files.testing.fs.PathBaseTest;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Stat.lstat;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathCreateSuccessTest extends PathBaseTest {

    private final PathCreation creation;
    private final String subPath;

    public PathCreateSuccessTest(PathCreation creation, String subPath) {
        this.creation = creation;
        this.subPath = subPath;
    }

    @Parameters(name = "{0}, \"{1}\"")
    public static Collection<Object[]> data() {
        String[] subPaths = {
                "a",
                " ",
                "\n",
                "hello world",
                "你好",
        };
        List<Object[]> data = new ArrayList<>();
        for (PathCreation creation : PathCreation.values()) {
            for (String subPath : subPaths) {
                data.add(new Object[]{creation, subPath});
            }
        }
        return data;
    }

    @Test
    public void create_success() throws Exception {
        creation.createUsingOurCodeAssertResult(dir1().concat(subPath));
    }

    @Test
    public void create_with_correct_default_permissions() throws Exception {

        Path actual = dir1().concat(subPath);
        File expected = new File(dir1().toString(), subPath + "_expected");
        creation.createUsingOurCodeAssertResult(actual);
        creation.createUsingSystemApi(expected);

        Stat stat = lstat(expected.getPath().getBytes());
        assertEquals(expected.canRead(), actual.isReadable());
        assertEquals(expected.canWrite(), actual.isWritable());
        assertEquals(expected.canExecute(), actual.isExecutable());
        assertEquals(stat.permissions(), actual.stat(NOFOLLOW).permissions());
    }

}
