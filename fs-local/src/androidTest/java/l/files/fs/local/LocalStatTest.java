package l.files.fs.local;

import android.os.Parcel;

import l.files.testing.fs.PathBaseTest;

import static l.files.fs.LinkOption.NOFOLLOW;

public final class LocalStatTest extends PathBaseTest {

    public LocalStatTest() {
        super(LocalFileSystem.INSTANCE);
    }

    public void test_can_create_from_parcel() throws Exception {
        Parcel parcel = Parcel.obtain();
        try {
            l.files.fs.Stat expected = fs.stat(dir1(), NOFOLLOW);
            expected.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            LocalStat actual = LocalStat.CREATOR.createFromParcel(parcel);
            assertEquals(expected, actual);
        } finally {
            parcel.recycle();
        }
    }
}
