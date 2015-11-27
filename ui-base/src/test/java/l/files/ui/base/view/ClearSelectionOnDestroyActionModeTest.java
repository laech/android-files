package l.files.ui.base.view;

import org.junit.Test;

import l.files.ui.base.selection.Selection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ClearSelectionOnDestroyActionModeTest {

    @Test
    public void clears_selection_on_destroy_action_mode() throws Exception {
        Selection.Callback callback = mock(Selection.Callback.class);
        Selection<Integer, String> selection = new Selection<>();
        selection.add(100, "100");
        selection.addWeaklyReferencedCallback(callback);
        assertEquals(1, selection.size());

        new ClearSelectionOnDestroyActionMode(selection).onDestroyActionMode(null);

        assertEquals(0, selection.size());
        verify(callback).onSelectionChanged();
    }

}
