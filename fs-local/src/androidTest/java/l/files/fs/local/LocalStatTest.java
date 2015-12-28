package l.files.fs.local;

import android.os.Parcel;

import org.junit.Test;

import l.files.fs.Files;
import l.files.fs.Path;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Stat.stat;
import static org.junit.Assert.assertEquals;

public final class LocalStatTest extends PathBaseTest {

    @Test
    public void local_stat() throws Exception {
        Path link = Files.createSymbolicLink(dir1().resolve("link"), dir2());
        assertEquals(lstat(link.toByteArray()), Files.stat(link, NOFOLLOW));
        assertEquals(stat(link.toByteArray()), Files.stat(link, FOLLOW));
    }

    @Test
    public void can_create_from_parcel() throws Exception {
        Parcel parcel = Parcel.obtain();
        try {
            l.files.fs.Stat expected = Files.stat(dir1(), NOFOLLOW);
            expected.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            Stat actual = Stat.CREATOR.createFromParcel(parcel);
            assertEquals(expected, actual);
        } finally {
            parcel.recycle();
        }
    }
}
