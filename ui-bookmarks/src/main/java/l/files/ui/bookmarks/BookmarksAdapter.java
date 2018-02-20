package l.files.ui.bookmarks;

import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import l.files.fs.Path;
import l.files.ui.base.fs.FileIcons;
import l.files.ui.base.fs.FileLabels;
import l.files.ui.base.fs.OpenFileEvent;
import l.files.ui.base.messaging.MainThreadTopic;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.selection.SelectionModeViewHolder;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.widget.ItemViewHolder;
import l.files.ui.base.widget.StableAdapter;

import static java.util.Collections.emptyList;
import static l.files.base.Objects.requireNonNull;

final class BookmarksAdapter extends StableAdapter<Object, ItemViewHolder<Object>> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_BOOKMARK = 1;

    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;
    private final Selection<Path, Path> selection;
    private final MainThreadTopic<OpenFileEvent> topic;

    BookmarksAdapter(
            Selection<Path, Path> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback,
            MainThreadTopic<OpenFileEvent> topic
    ) {
        this.topic = requireNonNull(topic);
        this.selection = requireNonNull(selection);
        this.actionModeProvider = requireNonNull(actionModeProvider);
        this.actionModeCallback = requireNonNull(actionModeCallback);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof Path ? VIEW_TYPE_BOOKMARK : VIEW_TYPE_HEADER;
    }

    @Override
    public ItemViewHolder<Object> onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        @SuppressWarnings("unchecked")
        ItemViewHolder<Object> holder = (ItemViewHolder<Object>)
                (viewType == VIEW_TYPE_BOOKMARK
                        ? newBookmarkHolder(parent, inflater)
                        : newHeaderHolder(parent, inflater));
        return holder;
    }

    private HeaderHolder newHeaderHolder(ViewGroup parent, LayoutInflater inflater) {
        return new HeaderHolder(inflater.inflate(R.layout.bookmark_header, parent, false));
    }

    private BookmarkHolder newBookmarkHolder(ViewGroup parent, LayoutInflater inflater) {
        return new BookmarkHolder(inflater.inflate(R.layout.bookmark_item, parent, false));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(ItemViewHolder<Object> holder, int position) {
        holder.bind(getItem(position), emptyList());
    }

    @Override
    public Object getItemIdObject(Object item) {
        return item;
    }

    private static class HeaderHolder extends ItemViewHolder<String> {

        private final TextView headerView;

        HeaderHolder(View itemView) {
            super(itemView);
            this.headerView = itemView.findViewById(R.id.header);
        }

        @Override
        public void bind(String header, List<Object> payloads) {
            super.bind(header, payloads);
            headerView.setText(header);
        }
    }

    private final class BookmarkHolder extends SelectionModeViewHolder<Path, Path> {

        private final TextView titleView;
        private final ImageView iconView;

        BookmarkHolder(View itemView) {
            super(itemView, selection, actionModeProvider, actionModeCallback);
            this.titleView = itemView.findViewById(R.id.title);
            this.iconView = itemView.findViewById(R.id.icon);
        }

        @Override
        protected Path itemId(Path file) {
            return file;
        }

        @Override
        public void bind(Path path, List<Object> payloads) {
            super.bind(path, payloads);
            titleView.setText(FileLabels.get(resources(), path));
            iconView.setImageResource(FileIcons.getDirectory(path));
        }

        @Override
        protected void onClick(View v, Path path) {
            topic.postOnMainThread(new OpenFileEvent(path, null));
        }
    }
}
