package l.files.fs.local;

import android.os.Parcel;

import java.io.File;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

import static l.files.fs.LinkOption.NOFOLLOW;

public final class LocalStatTest extends PathBaseTest {

    @Override
    protected Path create(File file) {
        return LocalPath.fromFile(file);
    }

    public void test_can_create_from_parcel() throws Exception {
        Parcel parcel = Parcel.obtain();
        try {
            l.files.fs.Stat expected = dir1().stat(NOFOLLOW);
            expected.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            LocalStat actual = LocalStat.CREATOR.createFromParcel(parcel);
            assertEquals(expected, actual);
        } finally {
            parcel.recycle();
        }
    }
}
