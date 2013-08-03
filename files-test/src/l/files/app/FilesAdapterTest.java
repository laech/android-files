package l.files.app;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.test.AndroidTestCase;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.base.Function;

import java.io.File;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class FilesAdapterTest extends AndroidTestCase {

  private Function<File, String> names;
  private Function<File, Drawable> drawables;

  private ListView list;
  private File file;
  private FilesAdapter adapter;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    list = new ListView(getContext());
    file = mock(File.class);
    names = mock(Function.class);
    drawables = mock(Function.class);
    adapter = new FilesAdapter(names, drawables, mock(Function.class));
    adapter.replace(list, asList(file), false);
  }

  /*
   * If container view is disabled, on selection of the item the background is
   * grey.
   */
  public void testGetView_containerViewIsDisabledIfFileCanNotBeRead() {
    given(file.canRead()).willReturn(false);
    assertFalse(getView().isEnabled());
  }

  public void testGetView_containerViewIsEnabledIfFileCanBeRead() {
    given(file.canRead()).willReturn(true);
    assertTrue(getView().isEnabled());
  }

  public void testGetView_titleViewShowsFileName() {
    given(names.apply(file)).willReturn("test");
    assertEquals("test", ((TextView) findView(android.R.id.title)).getText());
  }

  public void testGetView_titleViewShowsIcon() {
    ColorDrawable drawable = new ColorDrawable();
    given(drawables.apply(file)).willReturn(drawable);
    TextView text = findView(android.R.id.title);
    assertEquals(
        asList(drawable, null, null, null),
        asList(text.getCompoundDrawables()));
  }

  /*
   * If the title view is disabled, the file name will be grey out.
   */
  public void testGetView_titleViewIsDisabledIfFileCanNotBeRead() {
    given(file.canRead()).willReturn(false);
    assertFalse(findView(android.R.id.title).isEnabled());
  }

  public void testGetView_titleViewIsEnabledIfFileCanBeRead() {
    given(file.canRead()).willReturn(true);
    assertTrue(findView(android.R.id.title).isEnabled());
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

  @SuppressWarnings("unchecked")
  private <T extends View> T findView(int id) {
    return (T) getView().findViewById(id);
  }
}
