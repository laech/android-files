package l.files.features.objects.action;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import java.util.Objects;

import l.files.ui.StableAdapter;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

// TODO investigate replacement from testing support lib
public abstract class StableRecyclerViewAction<R> implements Action<R>
{
    private final RecyclerView recycler;

    protected StableRecyclerViewAction(final RecyclerView recycler)
    {
        checkNotNull(recycler, "recycler");
        checkArgument(recycler.getAdapter() instanceof StableAdapter);
        this.recycler = recycler;
    }

    public static Action<Boolean> willClick(final RecyclerView recycler)
    {
        return new StableRecyclerViewAction<Boolean>(recycler)
        {
            @Override
            protected Boolean perform(final Object item, final View view)
            {
                assertTrue(view.performClick());
                return true;
            }
        };
    }

    public static Action<View> willReturnView(final RecyclerView recycler)
    {
        return new StableRecyclerViewAction<View>(recycler)
        {
            @Override
            protected View perform(final Object item, final View view)
            {
                return view;
            }
        };
    }

    @Override
    public R action(final Object id)
    {
        @SuppressWarnings("unchecked")
        final StableAdapter<Object, ViewHolder> adapter =
                (StableAdapter<Object, ViewHolder>) recycler.getAdapter();

        for (int i = 0; i < adapter.getItemCount(); i++)
        {
            recycler.scrollToPosition(i);
            for (int j = 0; j < recycler.getChildCount(); j++)
            {
                final View child = recycler.getChildAt(j);
                final ViewHolder holder = recycler.getChildViewHolder(child);
                final int position = holder.getAdapterPosition();
                if (position == NO_POSITION)
                {
                    fail();
                }
                if (position == i)
                {
                    final Object thatId = adapter.getItemIdObject(position);
                    if (Objects.equals(id, thatId))
                    {
                        return perform(id, child);
                    }
                }
            }
        }

        fail(String.valueOf(id));
        return null;
    }

    protected abstract R perform(final Object item, final View view);

}
