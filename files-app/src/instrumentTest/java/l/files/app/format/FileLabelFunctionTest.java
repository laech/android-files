package l.files.app.format;

import static l.files.ui.UserDirs.DIR_HOME;
import static l.files.ui.UserDirs.DIR_ROOT;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import android.content.res.Resources;
import android.os.Build;
import java.io.File;
import l.files.R;
import l.files.test.BaseTest;

public final class FileLabelFunctionTest extends BaseTest {

  private Resources res;
  private FileLabelFunction labels;

  @Override protected void setUp() throws Exception {
    super.setUp();
    res = mock(Resources.class);
    labels = new FileLabelFunction(res);
  }

  public void testGetsNameForHomeDirectory() {
    given(res.getString(R.string.home)).willReturn("1");
    assertEquals("1", labels.apply(DIR_HOME));
  }

  public void testGetsNameForRootDirectory() {
    assertEquals(Build.MODEL, labels.apply(DIR_ROOT));
  }

  public void testGetsNameOfFileForNormalFile() {
    assertEquals("abc", labels.apply(new File("abc")));
  }
}
