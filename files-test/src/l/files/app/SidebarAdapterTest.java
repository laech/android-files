package l.files.app;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.test.AndroidTestCase;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.base.Function;

import java.io.File;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class SidebarAdapterTest extends AndroidTestCase {

  private Function<File, Drawable> drawables;
  private Function<File, String> labels;

  private SidebarAdapter adapter;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    labels = mock(Function.class);
    drawables = mock(Function.class);
    adapter = new SidebarAdapter(labels, drawables);
  }

  public void testIsEnabled_trueIfItemIsFile() {
    adapter.add(mock(File.class));
    assertTrue(adapter.isEnabled(0));
  }

  public void testIsEnabled_falseIfItemIsHeader() {
    adapter.add("hello");
    assertFalse(adapter.isEnabled(0));
  }

  public void testGetView_forFile() {
    File file = setFile();
    Drawable drawable = setDrawable(file);

    adapter.add(file);

    TextView view = getTitleView();
    assertEquals("abc", view.getText());
    assertEquals(
        asList(drawable, null, null, null),
        asList(view.getCompoundDrawables()));
  }

  public void testGetView_forHeader() {
    adapter.add("header");
    TextView view = getTitleView();
    assertEquals("header", view.getText());
    assertEquals(
        asList(null, null, null, null),
        asList(view.getCompoundDrawables()));
  }

  private TextView getTitleView() {
    return (TextView) adapter.getView(0, null, new ListView(getContext()))
        .findViewById(android.R.id.title);
  }

  private File setFile() {
    File file = mock(File.class);
    given(labels.apply(file)).willReturn("abc");
    return file;
  }

  private Drawable setDrawable(File file) {
    Drawable drawable = new ColorDrawable();
    given(drawables.apply(file)).willReturn(drawable);
    return drawable;
  }
}
