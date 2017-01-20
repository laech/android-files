package l.files.fs;

import android.os.Parcel;

import l.files.testing.fs.PathBaseTest;

import static l.files.fs.LinkOption.NOFOLLOW;

public final class StatTest extends PathBaseTest {

    public void test_can_create_from_parcel() throws Exception {
        Parcel parcel = Parcel.obtain();
        try {
            Stat expected = dir1().stat(NOFOLLOW);
            expected.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            Stat actual = Stat.CREATOR.createFromParcel(parcel);
            assertEquals(expected, actual);
        } finally {
            parcel.recycle();
        }
    }
}
