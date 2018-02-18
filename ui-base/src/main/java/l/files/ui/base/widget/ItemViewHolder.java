package l.files.ui.base.widget;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import java.util.List;

public abstract class ItemViewHolder<T> extends ViewHolder {

    private T item;

    public ItemViewHolder(View itemView) {
        super(itemView);
    }

    protected final T item() {
        return item;
    }

    @CallSuper
    public void bind(T item, List<Object> payloads) {
        this.item = item;
    }

    protected final Context context() {
        return itemView.getContext();
    }

    protected final Resources resources() {
        return itemView.getResources();
    }
}
