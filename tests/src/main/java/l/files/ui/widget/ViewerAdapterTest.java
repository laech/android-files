package l.files.ui.widget;

import android.view.View;
import android.view.ViewGroup;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public final class ViewerAdapterTest extends TestCase {

  private List<Object> items;
  private ViewerAdapter adapter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    items = newArrayList();
    adapter = create();
  }

  @SuppressWarnings("unchecked")
  public void testGetViewTypeCount_returnsTheNumberOfViewers() {
    adapter.addViewer(String.class, mock(Viewer.class));
    adapter.addViewer(Integer.class, mock(Viewer.class));
    adapter.addViewer(Boolean.class, mock(Viewer.class));
    assertThat(adapter.getViewTypeCount()).isEqualTo(3);
  }

  @SuppressWarnings("unchecked")
  public void testGetItemViewType_returnsDifferentValueForEachViewer() {
    adapter.addViewer(String.class, mock(Viewer.class));
    adapter.addViewer(Integer.class, mock(Viewer.class));
    items.add("a");
    items.add("b");
    items.add(1);
    assertThat(adapter.getItemViewType(0)).isEqualTo(0);
    assertThat(adapter.getItemViewType(1)).isEqualTo(0);
    assertThat(adapter.getItemViewType(2)).isEqualTo(1);
  }

  @SuppressWarnings("unchecked")
  public void testGetItemViewType_supportsItemOfSubclass() {
    items.add(new ArrayList<Object>());
    adapter.addViewer(Collection.class, mock(Viewer.class));
    assertThat(adapter.getItemViewType(0)).isEqualTo(0);
  }

  @SuppressWarnings("unchecked")
  public void testGetItemViewType_usesViewerOfMostSpecificMatch() {
    adapter.addViewer(Object.class, mock(Viewer.class));
    adapter.addViewer(Collection.class, mock(Viewer.class));
    adapter.addViewer(List.class, mock(Viewer.class));

    items.add(emptyList());

    assertThat(adapter.getItemViewType(0)).isEqualTo(2);
  }

  @SuppressWarnings("unchecked")
  public void testGetView_supportsItemOfSubclass() {
    String item = "string";
    items.add(item);

    View view = mock(View.class);
    Viewer<Object> viewer = mock(Viewer.class);
    given(viewer.getView(item, null, null)).willReturn(view);
    adapter.addViewer(Object.class, viewer);

    assertThat(adapter.getView(0, null, null)).isSameAs(view);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testGetView_usesViewerOfMostSpecificMatch() {
    Viewer<Object> objectViewer = mock(Viewer.class);
    Viewer<Collection> collectionViewer = mock(Viewer.class);
    Viewer<List> listViewer = mock(Viewer.class);
    adapter.addViewer(Object.class, objectViewer);
    adapter.addViewer(List.class, listViewer);
    adapter.addViewer(Collection.class, collectionViewer);

    items.add(emptyList());
    adapter.getView(0, null, null);

    verifyZeroInteractions(objectViewer);
    verifyZeroInteractions(collectionViewer);
    verify(listViewer)
        .getView(any(List.class), any(View.class), any(ViewGroup.class));
  }

  private ViewerAdapter create() {
    return new ViewerAdapter() {
      @Override public int getCount() {
        return items.size();
      }

      @Override public Object getItem(int position) {
        return items.get(position);
      }

      @Override public long getItemId(int position) {
        return position;
      }
    };
  }
}
