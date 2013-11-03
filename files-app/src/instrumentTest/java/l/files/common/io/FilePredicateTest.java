package l.files.common.io;

import static l.files.common.io.FilePredicate.CAN_READ;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;
import l.files.test.BaseTest;

public final class FilePredicateTest extends BaseTest {

  public void testCanRead_trueForReadableFile() {
    File file = mock(File.class);
    given(file.canRead()).willReturn(true);
    assertTrue(CAN_READ.apply(file));
  }

  public void testCanRead_falseForUnreadableFile() {
    File file = mock(File.class);
    given(file.canRead()).willReturn(false);
    assertFalse(CAN_READ.apply(file));
  }
}
