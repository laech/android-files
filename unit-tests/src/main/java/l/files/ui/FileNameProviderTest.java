package l.files.ui;

import junit.framework.TestCase;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class FileNameProviderTest extends TestCase {

  private FileNameProvider function;

  @Override protected void setUp() throws Exception {
    super.setUp();
    function = new FileNameProvider();
  }

  public void testGetsNameOfFile() {
    File file = mock(File.class);
    given(file.getName()).willReturn("a");
    assertThat(function.apply(file)).isEqualTo("a");
  }
}
