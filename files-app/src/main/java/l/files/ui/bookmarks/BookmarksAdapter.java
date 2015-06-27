package l.files.ui.bookmarks;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import l.files.R;
import l.files.common.view.ActionModeProvider;
import l.files.fs.Resource;
import l.files.ui.FileLabels;
import l.files.ui.Icons;
import l.files.ui.OpenFileRequest;
import l.files.ui.StableAdapter;
import l.files.ui.selection.Selection;
import l.files.ui.selection.SelectionModeViewHolder;

import static java.util.Objects.requireNonNull;
import static l.files.common.view.Views.find;
import static l.files.ui.Icons.directoryIconStringId;

final class BookmarksAdapter extends StableAdapter<Resource, BookmarksAdapter.Holder>
{
    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;
    private final Selection<Resource> selection;
    private final EventBus bus;

    BookmarksAdapter(
            final Selection<Resource> selection,
            final ActionModeProvider actionModeProvider,
            final ActionMode.Callback actionModeCallback,
            final EventBus bus)
    {
        this.bus = requireNonNull(bus, "bus");
        this.selection = requireNonNull(selection, "selection");
        this.actionModeProvider = requireNonNull(actionModeProvider, "actionModeProvider");
        this.actionModeCallback = requireNonNull(actionModeCallback, "actionModeCallback");
    }

    @Override
    public Holder onCreateViewHolder(
            final ViewGroup parent,
            final int viewType)
    {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.bookmark_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final Holder holder, final int position)
    {
        holder.bind(getItem(position));
    }

    @Override
    public Resource getItemIdObject(final int position)
    {
        return getItem(position);
    }

    final class Holder extends SelectionModeViewHolder<Resource, Resource>
    {
        final TextView title;
        final TextView icon;

        Holder(final View itemView)
        {
            super(itemView, selection, actionModeProvider, actionModeCallback);
            title = find(android.R.id.title, this);
            icon = find(android.R.id.icon, this);
            icon.setTypeface(Icons.font(itemView.getResources().getAssets()));
        }

        @Override
        protected Resource itemId(final Resource resource)
        {
            return resource;
        }

        @Override
        public void bind(final Resource resource)
        {
            super.bind(resource);
            title.setText(FileLabels.get(title.getResources(), resource));
            icon.setText(directoryIconStringId(resource));
        }

        @Override
        protected void onClick(final View v, final Resource item)
        {
            bus.post(OpenFileRequest.create(item));
        }
    }
}
