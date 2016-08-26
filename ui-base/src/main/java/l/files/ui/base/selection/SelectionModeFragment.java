package l.files.ui.base.selection;

import android.os.Bundle;
import android.support.v7.view.ActionMode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import l.files.ui.base.app.BaseFragment;
import l.files.ui.base.view.ActionModeProvider;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.identityHashCode;
import static l.files.base.Objects.requireNonNull;

public abstract class SelectionModeFragment<K, V> extends BaseFragment {

    private static final String SELECTION_ID = "selectionId";

    private static final Map<Integer, State> selections = new HashMap<>();

    @Nullable
    private Integer selectionId;

    @Nullable
    private Selection<K, V> selection;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSelection(savedInstanceState);
        cleanSelectionStates();
    }

    @SuppressWarnings("unchecked")
    private void setSelection(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            selectionId = savedInstanceState.getInt(SELECTION_ID);
            State state = selections.get(selectionId);
            if (state != null) {
                selection = (Selection<K, V>) state.selection;
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
            if (entry.getKey().equals(selectionId) ||
                    now - entry.getValue().creationTime > 10000) {
                it.remove();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        assert selection != null;
        assert selectionId != null;
        selections.put(selectionId, new State(selection));
        outState.putInt(SELECTION_ID, selectionId);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        assert selection != null;
        if (!selection.isEmpty()) {
            actionModeProvider().startSupportActionMode(actionModeCallback());
        }
    }

    protected Selection<K, V> selection() {
        assert selection != null;
        return selection;
    }

    protected abstract ActionMode.Callback actionModeCallback();

    protected abstract ActionModeProvider actionModeProvider();

    private static final class State {
        final Selection<?, ?> selection;
        final long creationTime;

        State(Selection<?, ?> selection) {
            this.selection = requireNonNull(selection);
            this.creationTime = currentTimeMillis();
        }
    }
}
