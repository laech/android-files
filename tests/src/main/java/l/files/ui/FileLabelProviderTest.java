package l.files.ui;

import static l.files.util.FileSystem.DIRECTORY_HOME;
import static l.files.util.FileSystem.DIRECTORY_ROOT;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;

import junit.framework.TestCase;
import l.files.R;
import android.content.res.Resources;
import android.os.Build;

public final class FileLabelProviderTest extends TestCase {

  private Resources res;
  private FileLabelProvider labels;

  @Override protected void setUp() throws Exception {
    super.setUp();
    res = mock(Resources.class);
    labels = new FileLabelProvider(res);
  }

  public void testGetsNameForHomeDirectory() {
    given(res.getString(R.string.home)).willReturn("1");
    assertThat(labels.apply(DIRECTORY_HOME)).isEqualTo("1");
  }

  public void testGetsNameForRootDirectory() {
    assertThat(labels.apply(DIRECTORY_ROOT)).isEqualTo(Build.MODEL);
  }

  public void testGetsNameOfFileForNormalFile() {
    assertThat(labels.apply(new File("abc"))).isEqualTo("abc");
  }

}
