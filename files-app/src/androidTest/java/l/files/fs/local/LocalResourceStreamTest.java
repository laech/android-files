package l.files.fs.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import l.files.fs.local.LocalResourceStream.Callback;

import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class LocalResourceStreamTest extends ResourceBaseTest {

    public void testReturnCorrectEntries() throws Exception {
        LocalResource a = dir1().resolve("a").createFile();
        LocalResource b = dir1().resolve("b").createDirectory();
        LocalResource c = dir1().resolve("c").createSymbolicLink(a);

        List<LocalPathEntry> actual = list(dir1());
        List<LocalPathEntry> expected = asList(
                LocalPathEntry.create(a, a.readStatus(NOFOLLOW).getInode(), false),
                LocalPathEntry.create(b, b.readStatus(NOFOLLOW).getInode(), true),
                LocalPathEntry.create(c, c.readStatus(NOFOLLOW).getInode(), false)
        );
        assertEquals(expected, actual);
    }

    private List<LocalPathEntry> list(final LocalResource dir) throws IOException {
        final List<LocalPathEntry> actual = new ArrayList<>();
        LocalResourceStream.list(dir1(), NOFOLLOW, new Callback() {
            @Override
            public boolean accept(long inode, String name, boolean directory) throws IOException {
                actual.add(LocalPathEntry.create(dir.resolve(name), inode, directory));
                return true;
            }
        });
        return actual;
    }

}
