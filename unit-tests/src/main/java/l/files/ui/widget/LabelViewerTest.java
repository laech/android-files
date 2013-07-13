package l.files.ui.widget;

import android.widget.TextView;
import com.google.common.base.Function;
import junit.framework.TestCase;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class LabelViewerTest extends TestCase {

  private int textViewId;
  private Function<Object, String> labels;
  private LabelViewer<Object> viewer;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    textViewId = 2;
    labels = mock(Function.class);
    viewer = new LabelViewer<Object>(textViewId, labels);
  }

  public void testSetsTextToTextView() {
    Object item = new Object();
    given(labels.apply(item)).willReturn("hello");
    TextView view = setTextView();

    viewer.getView(item, view, null);

    verify(view).findViewById(textViewId);
    verify(view).setText("hello");
  }

  private TextView setTextView() {
    TextView view = mock(TextView.class);
    given(view.findViewById(textViewId)).willReturn(view);
    return view;
  }
}
