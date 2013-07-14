package l.files.ui.app.sidebar;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.test.AndroidTestCase;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.base.Function;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;
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
    assertThat(adapter.isEnabled(0)).isTrue();
  }

  public void testIsEnabled_falseIfItemIsHeader() {
    adapter.add("hello");
    assertThat(adapter.isEnabled(0)).isFalse();
  }

  public void testGetView_forFile() {
    File file = setFile();
    Drawable drawable = setDrawable(file);

    adapter.add(file);

    TextView view = getTitleView();
    assertThat(view.getText()).isEqualTo("abc");
    assertThat(view.getCompoundDrawables())
        .containsExactly(drawable, null, null, null);
  }

  public void testGetView_forHeader() {
    adapter.add("header");
    TextView view = getTitleView();
    assertThat(view.getText()).isEqualTo("header");
    assertThat(view.getCompoundDrawables())
        .containsExactly(null, null, null, null);
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
