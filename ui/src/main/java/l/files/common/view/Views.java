package l.files.common.view;

import android.app.Activity;
import android.app.Fragment;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

public final class Views {
    private Views() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T find(final int id, final Activity activity) {
        return (T) activity.findViewById(id);
    }

    public static <T extends View> T find(final int id, final Fragment fragment) {
        return find(id, fragment.getView());
    }

    public static <T extends View> T find(final int id, final ViewHolder holder) {
        return find(id, holder.itemView);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T find(final int id, final View root) {
        return (T) root.findViewById(id);
    }
}
