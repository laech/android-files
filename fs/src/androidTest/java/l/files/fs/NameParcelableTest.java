package l.files.fs;

import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class NameParcelableTest {

    private final Name expected;

    public NameParcelableTest(String expected) {
        this.expected = Name.fromString(expected);
    }

    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
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
            Name actual = Name.CREATOR.createFromParcel(parcel);
            assertEquals(expected, actual);
        } finally {
            parcel.recycle();
        }
    }
}
