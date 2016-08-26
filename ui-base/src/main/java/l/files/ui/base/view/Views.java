package l.files.ui.base.view;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

public final class Views {
    private Views() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T find(int id, Activity activity) {
        return (T) activity.findViewById(id);
    }

    public static <T extends View> T find(int id, Fragment fragment) {
        View view = fragment.getView();
        if (view == null) {
            throw new IllegalStateException();
        }
        return find(id, view);
    }

    public static <T extends View> T find(int id, ViewHolder holder) {
        return find(id, holder.itemView);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T find(int id, View root) {
        return (T) root.findViewById(id);
    }
}
