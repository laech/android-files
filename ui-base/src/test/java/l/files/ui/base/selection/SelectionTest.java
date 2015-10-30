package l.files.ui.base.selection;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import l.files.ui.base.selection.Selection.Callback;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class SelectionTest {

    private Selection<Integer> selection;
    private Callback callback;

    @Before
    public void setUp() throws Exception {
        selection = new Selection<>();
        callback = mockCallback();
    }

    private Callback mockCallback() {
        return mock(Callback.class);
    }

    @Test
    public void add_ignores_duplicate() throws Exception {
        selection.add(7);
        selection.add(7);
        selection.addAll(asList(7, 7, 7));
        assertEquals(1, selection.size());
    }

    @Test
    public void can_add_selection_while_being_notified() throws Exception {
        final boolean[] called = {false};
        Callback callback1 = new Callback() {
            @Override
            public void onSelectionChanged() {
                selection.addWeaklyReferencedCallback(SelectionTest.this.callback);
            }
        };
        Callback callback2 = new Callback() {
            @Override
            public void onSelectionChanged() {
                called[0] = true;
                selection.add(1000);
            }
        };
        selection.addWeaklyReferencedCallback(callback1);
        selection.addWeaklyReferencedCallback(callback2);
        selection.add(1);
        assertTrue(called[0]);
    }

    @Test
    public void does_not_notify_after_callback_removal() throws Exception {
        selection.addWeaklyReferencedCallback(callback);
        selection.removeCallback(callback);
        selection.add(1);
        selection.addAll(asList(2, 3));
        selection.clear();
        verifyZeroInteractions(callback);
    }

    @Test
    public void does_not_notify_on_add_if_we_already_have_that_item() throws Exception {
        selection.add(1);
        selection.addWeaklyReferencedCallback(callback);
        selection.add(1);
        verifyZeroInteractions(callback);
    }

    @Test
    public void does_not_notify_on_add_all_if_selection_already_have_all_the_items() throws Exception {
        selection.add(1);
        selection.add(2);
        selection.addWeaklyReferencedCallback(callback);
        selection.addAll(asList(1, 2));
        verifyZeroInteractions(callback);
    }

    @Test
    public void does_not_notify_on_clear_if_already_empty() throws Exception {
        selection.addWeaklyReferencedCallback(callback);
        selection.clear();
        verifyZeroInteractions(callback);
    }

    @Test
    public void notifies_on_clear_if_selection_is_not_empty() throws Exception {
        selection.add(1);
        selection.addWeaklyReferencedCallback(callback);
        selection.clear();
        verify(callback).onSelectionChanged();
    }

    @Test
    public void notifies_on_add() throws Exception {
        selection.addWeaklyReferencedCallback(callback);
        selection.add(1);
        verify(callback).onSelectionChanged();
    }

    @Test
    public void notifies_on_add_all_if_selection_have_none_of_the_items() throws Exception {
        selection.addWeaklyReferencedCallback(callback);
        selection.addAll(asList(1, 2, 3));
        verify(callback).onSelectionChanged();
    }

    @Test
    public void notifies_on_add_all_if_selection_does_not_have_some_of_the_items() throws Exception {
        selection.add(1);
        selection.add(2);
        selection.addWeaklyReferencedCallback(callback);
        selection.addAll(asList(1, 2, 3));
        verify(callback).onSelectionChanged();
    }

    @Test
    public void notifies_on_toggle() throws Exception {
        selection.addWeaklyReferencedCallback(callback);
        selection.toggle(1);
        verify(callback).onSelectionChanged();

        selection.toggle(1);
        verify(callback, times(2)).onSelectionChanged();
    }

    @Test
    public void copy_returns_all_selections() throws Exception {
        selection.add(1);
        selection.add(2);
        selection.addAll(asList(3, 4, 5));
        assertEquals(new HashSet<>(asList(1, 2, 3, 4, 5)), selection.copy());
    }

    @Test
    public void copy_does_not_change_when_selection_changes() throws Exception {
        selection.add(1);
        selection.add(2);

        Set<Integer> copy = selection.copy();
        selection.add(3);
        assertEquals(new HashSet<>(asList(1, 2)), copy);
    }

    @Test
    public void copy_cannot_be_modified() throws Exception {
        selection.add(1);
        Set<Integer> copy = selection.copy();

        try {
            copy.clear();
            fail();
        } catch (UnsupportedOperationException e) {
            // Pass
        }
    }

    @Test
    public void toggle_removes_existing_selection() throws Exception {
        selection.add(1);
        assertTrue(selection.contains(1));

        selection.toggle(1);
        assertFalse(selection.contains(1));
    }

    @Test
    public void toggle_adds_selection() throws Exception {
        selection.toggle(1);
        assertTrue(selection.contains(1));
    }

    @Test
    public void toggle_toggle_removes_selection() throws Exception {
        selection.toggle(1);
        selection.toggle(1);
        assertFalse(selection.contains(1));
    }

    @Test
    public void toggle_toggle_toggle_adds_selection_back() throws Exception {
        selection.toggle(1);
        selection.toggle(1);
        selection.toggle(1);
        assertTrue(selection.contains(1));
    }

    @Test
    public void empty_initially() throws Exception {
        assertTrue(selection.isEmpty());
    }

    @Test
    public void empty_if_cleared() throws Exception {
        selection.add(1);
        selection.add(2);
        selection.clear();
        assertTrue(selection.isEmpty());
    }

}