package com.example.files.widget;

import android.widget.ArrayAdapter;
import junit.framework.TestCase;
import org.mockito.InOrder;

import static com.example.files.widget.ArrayAdapters.removeAll;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public final class ArrayAdaptersTest extends TestCase {

  private ArrayAdapter<Object> adapter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    adapter = mockAdapter();
  }

  public void testRemoveAll() {
    removeAll(adapter, asList(1, 2));

    InOrder order = inOrder(adapter);
    order.verify(adapter).setNotifyOnChange(false);
    order.verify(adapter).remove(1);
    order.verify(adapter).remove(2);
    order.verify(adapter).notifyDataSetChanged();
    order.verifyNoMoreInteractions();
  }

  @SuppressWarnings("unchecked")
  private ArrayAdapter<Object> mockAdapter() {
    return mock(ArrayAdapter.class);
  }

}
