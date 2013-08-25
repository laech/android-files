package l.files.app;

import static android.graphics.Typeface.MONOSPACE;
import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import android.graphics.Typeface;
import android.test.AndroidTestCase;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.base.Function;
import java.io.File;

public final class FilesAdapterTest extends AndroidTestCase {

  private Function<File, String> names;
  private Function<File, Typeface> fonts;

  private ListView list;
  private File file;
  private FilesAdapter adapter;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    list = new ListView(getContext());
    file = mock(File.class);
    names = mock(Function.class);
    fonts = mock(Function.class);
    adapter = new FilesAdapter(names, fonts, mock(Function.class), 100);
    adapter.replace(list, asList(file), false);
  }

  /*
   * If container view is disabled, on selection of the item the background is
   * grey.
   */
  public void testContainerViewIsDisabledIfFileCanNotBeRead() {
    testViewEnabled(false, android.R.id.content);
  }

  public void testContainerViewIsEnabledIfFileCanBeRead() {
    testViewEnabled(true, android.R.id.content);
  }

  public void testTitleViewShowsFileName() {
    given(names.apply(file)).willReturn("test");
    assertEquals("test", ((TextView) findView(android.R.id.title)).getText());
  }

  public void testIconViewShowsIcon() {
    given(fonts.apply(file)).willReturn(MONOSPACE);
    TextView text = findView(android.R.id.icon);
    assertEquals(MONOSPACE, text.getTypeface());
  }

  /*
   * If the title view is disabled, the file name will be grey out.
   */
  public void testTitleViewIsDisabledIfFileCanNotBeRead() {
    testViewEnabled(false, android.R.id.title);
  }

  public void testTitleViewIsEnabledIfFileCanBeRead() {
    testViewEnabled(true, android.R.id.title);
  }

  /*
   * If the icon view is disabled, icon will be of a different color.
   */
  public void testIconViewIsDisabledIfFileCanNotBeRead() {
    testViewEnabled(false, android.R.id.icon);
  }

  public void testIconViewIsEnabledIfFileCanBeRead() {
    testViewEnabled(true, android.R.id.icon);
  }

  public void testIsEnabled_trueForFile() {
    adapter.replace(list, asList(file), false);
    assertTrue(adapter.isEnabled(0));
  }

  public void testIsEnabled_falseForNonFile() {
    adapter.replace(list, asList(new Object()), false);
    assertFalse(adapter.isEnabled(0));
  }

  private View getView() {
    return adapter.getView(0, null, list);
  }

  @SuppressWarnings("unchecked") private <T extends View> T findView(int id) {
    return (T) getView().findViewById(id);
  }

  private void testViewEnabled(boolean enabled, final int id) {
    given(file.canRead()).willReturn(enabled);
    assertEquals(enabled, findView(id).isEnabled());
  }
}
