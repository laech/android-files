package l.files.ui.selection;

import com.google.common.collect.ImmutableSet;

import junit.framework.TestCase;

import java.util.Set;

import l.files.ui.selection.Selection.Callback;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class SelectionTest extends TestCase
{
    private Selection<Integer> selection;
    private Callback callback;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        selection = new Selection<>();
        callback = mockCallback();
    }

    private Callback mockCallback()
    {
        return mock(Callback.class);
    }

    public void test_add_ignores_duplicate() throws Exception
    {
        selection.add(7);
        selection.add(7);
        selection.addAll(asList(7, 7, 7));
        assertEquals(1, selection.size());
    }

    public void test_can_add_selection_while_being_notified() throws Exception
    {
        final boolean[] called = {false};
        final Callback callback1 = new Callback()
        {
            @Override
            public void onSelectionChanged()
            {
                selection.addWeaklyReferencedCallback(SelectionTest.this.callback);
            }
        };
        final Callback callback2 = new Callback()
        {
            @Override
            public void onSelectionChanged()
            {
                called[0] = true;
                selection.add(1000);
            }
        };
        selection.addWeaklyReferencedCallback(callback1);
        selection.addWeaklyReferencedCallback(callback2);
        selection.add(1);
        assertTrue(called[0]);
    }

    public void test_does_not_notify_after_callback_removal() throws Exception
    {
        selection.addWeaklyReferencedCallback(callback);
        selection.removeCallback(callback);
        selection.add(1);
        selection.addAll(asList(2, 3));
        selection.clear();
        verifyZeroInteractions(callback);
    }

    public void test_does_not_notify_on_add_if_we_already_have_that_item() throws Exception
    {
        selection.add(1);
        selection.addWeaklyReferencedCallback(callback);
        selection.add(1);
        verifyZeroInteractions(callback);
    }

    public void test_does_not_notify_on_add_all_if_selection_already_have_all_the_items() throws Exception
    {
        selection.add(1);
        selection.add(2);
        selection.addWeaklyReferencedCallback(callback);
        selection.addAll(asList(1, 2));
        verifyZeroInteractions(callback);
    }

    public void test_does_not_notify_on_clear_if_already_empty() throws Exception
    {
        selection.addWeaklyReferencedCallback(callback);
        selection.clear();
        verifyZeroInteractions(callback);
    }

    public void test_notifies_on_clear_if_selection_is_not_empty() throws Exception
    {
        selection.add(1);
        selection.addWeaklyReferencedCallback(callback);
        selection.clear();
        verify(callback).onSelectionChanged();
    }

    public void test_notifies_on_add() throws Exception
    {
        selection.addWeaklyReferencedCallback(callback);
        selection.add(1);
        verify(callback).onSelectionChanged();
    }

    public void test_notifies_on_add_all_if_selection_have_none_of_the_items() throws Exception
    {
        selection.addWeaklyReferencedCallback(callback);
        selection.addAll(asList(1, 2, 3));
        verify(callback).onSelectionChanged();
    }

    public void test_notifies_on_add_all_if_selection_does_not_have_some_of_the_items() throws Exception
    {
        selection.add(1);
        selection.add(2);
        selection.addWeaklyReferencedCallback(callback);
        selection.addAll(asList(1, 2, 3));
        verify(callback).onSelectionChanged();
    }

    public void test_notifies_on_toggle() throws Exception
    {
        selection.addWeaklyReferencedCallback(callback);
        selection.toggle(1);
        verify(callback).onSelectionChanged();

        selection.toggle(1);
        verify(callback, times(2)).onSelectionChanged();
    }

    public void test_copy_returns_all_selections() throws Exception
    {
        selection.add(1);
        selection.add(2);
        selection.addAll(asList(3, 4, 5));
        assertEquals(ImmutableSet.of(1, 2, 3, 4, 5), selection.copy());
    }

    public void test_copy_does_not_change_when_selection_changes() throws Exception
    {
        selection.add(1);
        selection.add(2);

        final Set<Integer> copy = selection.copy();
        selection.add(3);
        assertEquals(ImmutableSet.of(1, 2), copy);
    }

    public void test_copy_cannot_be_modified() throws Exception
    {
        selection.add(1);
        final Set<Integer> copy = selection.copy();

        try
        {
            copy.clear();
            fail();
        }
        catch (final UnsupportedOperationException e)
        {
            // Pass
        }
    }

    public void test_toggle_removes_existing_selection() throws Exception
    {
        selection.add(1);
        assertTrue(selection.contains(1));

        selection.toggle(1);
        assertFalse(selection.contains(1));
    }

    public void test_toggle_adds_selection() throws Exception
    {
        selection.toggle(1);
        assertTrue(selection.contains(1));
    }

    public void test_toggle_toggle_removes_selection() throws Exception
    {
        selection.toggle(1);
        selection.toggle(1);
        assertFalse(selection.contains(1));
    }

    public void test_toggle_toggle_toggle_adds_selection_back() throws Exception
    {
        selection.toggle(1);
        selection.toggle(1);
        selection.toggle(1);
        assertTrue(selection.contains(1));
    }

    public void test_empty_initially() throws Exception
    {
        assertTrue(selection.isEmpty());
    }

    public void test_empty_if_cleared() throws Exception
    {
        selection.add(1);
        selection.add(2);
        selection.clear();
        assertTrue(selection.isEmpty());
    }

}
