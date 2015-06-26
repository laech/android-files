package l.files.common.widget;

import android.view.ActionMode.Callback;

public final class ActionModes
{
    private ActionModes()
    {
    }

    public static CompositeItem compose(final Callback... callbacks)
    {
        return new CompositeItem(callbacks);
    }
}
