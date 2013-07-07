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
    adapter = new SidebarAdapter(getContext(), drawables, labels);
  }

  public void testIsEnabled_trueIfItemIsFile() {
    adapter.add(mock(File.class));
    assertThat(adapter.isEnabled(0)).isTrue();
  }

  public void testIsEnabled_falseIfItemIsHeader() {
    adapter.add("hello");
    assertThat(adapter.isEnabled(0)).isFalse();
  }

  public void testGetItemViewType_forFile() {
    adapter.add(mock(File.class));
    assertThat(adapter.getItemViewType(0)).isEqualTo(0);
  }

  public void testGetItemViewType_forHeader() {
    adapter.add("header");
    assertThat(adapter.getItemViewType(0)).isEqualTo(1);
  }

  public void testGetViewTypeCount_is2ForFileAndHeader() {
    assertThat(adapter.getViewTypeCount()).isEqualTo(2);
  }

  public void testGetView_forFile() {
    File file = mock(File.class);
    given(labels.apply(file)).willReturn("abc");

    Drawable drawable = new ColorDrawable();
    given(drawables.apply(file)).willReturn(drawable);

    adapter.add(file);

    TextView view = (TextView) adapter.getView(0, null, new ListView(getContext()));
    assertThat(view.getText()).isEqualTo("abc");
    assertThat(view.getCompoundDrawables())
        .containsExactly(drawable, null, null, null);
  }

  public void testGetView_forHeader() {
    adapter.add("header");
    TextView view = (TextView) adapter.getView(0, null, new ListView(getContext()));
    assertThat(view.getText()).isEqualTo("header");
  }

}
