package l.files.operations.ui;

import android.os.Parcel;

import junit.framework.TestCase;

import java.util.Arrays;

public final class FailureMessageTest extends TestCase {

  public void testGettersReturnCorrectProperties() {
    FailureMessage msg = FailureMessage.create("my.path", "my.message");
    assertEquals("my.path", msg.path());
    assertEquals("my.message", msg.message());
  }

  public void testCanBeParceled() {
    FailureMessage expected = FailureMessage.create("path", "message");
    Parcel parcel = Parcel.obtain();
    try {
      expected.writeToParcel(parcel, 0);
      parcel.setDataPosition(0);
      assertEquals(expected, FailureMessage.CREATOR.createFromParcel(parcel));
    } finally {
      parcel.recycle();
    }
  }

  public void testParcelCreatesArrayWithCorrectLength() {
    FailureMessage[] array = FailureMessage.CREATOR.newArray(2);
    assertTrue(Arrays.equals(array, new FailureMessage[2]));
  }

}
