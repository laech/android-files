package l.files.common.io;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;
import l.files.test.BaseTest;

public final class FileNameFunctionTest extends BaseTest {

  public void testGetsNameOfFile() {
    File file = mock(File.class);
    given(file.getName()).willReturn("a");
    assertEquals("a", FileNameFunction.INSTANCE.apply(file));
  }
}
