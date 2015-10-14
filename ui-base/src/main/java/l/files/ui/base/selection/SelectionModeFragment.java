package l.files.ui.base.selection;

import android.os.Bundle;
import android.view.ActionMode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import l.files.ui.base.app.BaseFragment;
import l.files.ui.base.view.ActionModeProvider;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.identityHashCode;

public abstract class SelectionModeFragment<T> extends BaseFragment {

    private static final String SELECTION_ID = "selectionId";

    private static final Map<Integer, State> selections = new HashMap<>();

    private Integer selectionId;
    private Selection<T> selection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSelection(savedInstanceState);
        cleanSelectionStates();
    }

    @SuppressWarnings("unchecked")
    private void setSelection(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            selectionId = savedInstanceState.getInt(SELECTION_ID);
            State state = selections.get(selectionId);
            if (state != null) {
                selection = (Selection<T>) state.selection;
            }
        }

        if (selection == null) {
            selection = new Selection<>();
            selectionId = identityHashCode(selection);
            selections.put(selectionId, new State(selection));
        }
    }

    private void cleanSelectionStates() {
        long now = currentTimeMillis();
        Iterator<Entry<Integer, State>> it = selections.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, State> entry = it.next();
            if (entry.getKey().intValue() == selectionId ||
                    now - entry.getValue().creationTime > 10000) {
                it.remove();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        selections.put(selectionId, new State(selection));
        outState.putInt(SELECTION_ID, selectionId);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (!selection.isEmpty()) {
            actionModeProvider().startActionMode(actionModeCallback());
        }
    }

    protected Selection<T> selection() {
        return selection;
    }

    protected abstract ActionMode.Callback actionModeCallback();

    protected abstract ActionModeProvider actionModeProvider();

    private static final class State {
        final Selection<?> selection;
        final long creationTime;

        State(Selection<?> selection) {
            this.selection = selection;
            this.creationTime = currentTimeMillis();
        }
    }
}
