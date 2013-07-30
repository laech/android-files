package l.files.common.io;

import junit.framework.TestCase;

import java.io.File;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class FileNameFunctionTest extends TestCase {

  public void testGetsNameOfFile() {
    File file = mock(File.class);
    given(file.getName()).willReturn("a");
    assertEquals("a", FileNameFunction.INSTANCE.apply(file));
  }
}
