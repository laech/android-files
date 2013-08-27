package l.files.app;

import static android.graphics.Typeface.MONOSPACE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import android.graphics.Typeface;
import android.test.AndroidTestCase;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.base.Function;
import java.io.File;

public final class SidebarAdapterTest extends AndroidTestCase {

  private Function<File, Typeface> drawables;
  private Function<File, String> labels;

  private SidebarAdapter adapter;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    labels = mock(Function.class);
    drawables = mock(Function.class);
    adapter = new SidebarAdapter(labels, drawables);
  }

  public void testFileIsEnabled() {
    testEnabled(true, mock(File.class));
  }

  public void testHeaderIsNotEnabled() {
    testEnabled(false, "hello");
  }

  private void testEnabled(boolean enabled, Object obj) {
    adapter.add(obj);
    assertEquals(enabled, adapter.isEnabled(adapter.getCount() - 1));
  }

  public void testGetsFileView() {
    File file = setFile();
    Typeface typeface = setIconFont(file);

    adapter.add(file);

    TextView view = getIconView();
    assertEquals(typeface, view.getTypeface());
  }

  public void testGetsHeaderView() {
    adapter.add("header");
    TextView view = getTitleView();
    assertEquals("header", view.getText());
  }

  private TextView getTitleView() {
    return findView(android.R.id.title);
  }

  private TextView getIconView() {
    return findView(android.R.id.icon);
  }

  @SuppressWarnings("unchecked")
  private <T extends View> T findView(int id) {
    return (T) getView().findViewById(id);
  }

  private View getView() {
    return adapter.getView(0, null, new ListView(getContext()));
  }

  private File setFile() {
    File file = mock(File.class);
    given(labels.apply(file)).willReturn("abc");
    return file;
  }

  private Typeface setIconFont(File file) {
    given(drawables.apply(file)).willReturn(MONOSPACE);
    return MONOSPACE;
  }
}
