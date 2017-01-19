package l.files.fs.local;

import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import l.files.fs.Path;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathParcelableTest {

    private final Path expected;

    public PathParcelableTest(String expected) {
        this.expected = LocalPath.create(expected);
    }

    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {"/abc"},
                {"def"},
                {""},
                {"."},
                {".."},
                {"..."},
        });
    }

    @Test
    public void parcelable() throws Exception {
        Parcel parcel = Parcel.obtain();
        try {
            expected.writeToParcel(parcel, 0);
            Path actual = LocalPath.CREATOR.createFromParcel(parcel);
            assertEquals(expected, actual);
        } finally {
            parcel.recycle();
        }
    }
}
