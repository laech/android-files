package l.files.app.format;

import android.content.res.Resources;
import android.os.Build;
import junit.framework.TestCase;
import l.files.R;

import java.io.File;

import static l.files.app.UserDirs.DIR_HOME;
import static l.files.app.UserDirs.DIR_ROOT;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class FileLabelFunctionTest extends TestCase {

  private Resources res;
  private FileLabelFunction labels;

  @Override protected void setUp() throws Exception {
    super.setUp();
    res = mock(Resources.class);
    labels = new FileLabelFunction(res);
  }

  public void testGetsNameForHomeDirectory() {
    given(res.getString(R.string.home)).willReturn("1");
    assertThat(labels.apply(DIR_HOME)).isEqualTo("1");
  }

  public void testGetsNameForRootDirectory() {
    assertThat(labels.apply(DIR_ROOT)).isEqualTo(Build.MODEL);
  }

  public void testGetsNameOfFileForNormalFile() {
    assertThat(labels.apply(new File("abc"))).isEqualTo("abc");
  }
}
