package com.example.files.widget;

import android.widget.ArrayAdapter;
import junit.framework.TestCase;
import org.mockito.InOrder;

import static com.example.files.widget.ArrayAdapters.removeAll;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public final class ArrayAdaptersTest extends TestCase {

    private ArrayAdapter<Object> mAdapter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAdapter = mockAdapter();
    }

    public void testRemoveAll() {
        removeAll(mAdapter, asList(1, 2));

        InOrder order = inOrder(mAdapter);
        order.verify(mAdapter).setNotifyOnChange(false);
        order.verify(mAdapter).remove(1);
        order.verify(mAdapter).remove(2);
        order.verify(mAdapter).notifyDataSetChanged();
        order.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unchecked")
    private ArrayAdapter<Object> mockAdapter() {
        return mock(ArrayAdapter.class);
    }

}
