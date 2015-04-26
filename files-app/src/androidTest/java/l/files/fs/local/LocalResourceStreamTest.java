package l.files.fs.local;

import android.system.Os;
import android.system.StructStat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l.files.fs.Resource;
import l.files.fs.local.LocalResourceStream.Callback;

import static android.system.OsConstants.S_ISDIR;
import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class LocalResourceStreamTest extends ResourceBaseTest {

    public void testReturnCorrectEntries() throws Exception {
        Resource a = dir1().resolve("a").createFile();
        Resource b = dir1().resolve("b").createDirectory();
        Resource c = dir1().resolve("c").createSymbolicLink(a);

        List<Map<Object, Object>> actual = list(dir1());
        List<Map<Object, Object>> expected = asList(map(a), map(b), map(c));
        assertEquals(expected, actual);
    }

    private List<Map<Object, Object>> list(final Resource dir) throws IOException {
        final List<Map<Object, Object>> actual = new ArrayList<>();
        LocalResourceStream.list(dir, NOFOLLOW, new Callback() {
            @Override
            public boolean accept(long inode, String name, boolean directory) throws IOException {
                actual.add(map(inode, name, directory));
                return true;
            }
        });
        return actual;
    }

    private Map<Object, Object> map(Resource resource) throws Exception {
        StructStat stat = Os.lstat(resource.getPath());
        return map(stat.st_ino, resource.getName(), S_ISDIR(stat.st_mode));
    }

    private Map<Object, Object> map(long inode, String name, boolean directory) {
        Map<Object, Object> map = new HashMap<>();
        map.put("inode", inode);
        map.put("name", name);
        map.put("directory", directory);
        return map;
    }

}
