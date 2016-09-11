package l.files.ui.bookmarks;

import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import l.files.fs.Path;
import l.files.ui.base.fs.FileIcons;
import l.files.ui.base.fs.FileLabels;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.selection.SelectionModeViewHolder;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.widget.StableAdapter;

import static java.util.Collections.emptyList;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.base.view.Views.find;

final class BookmarksAdapter extends StableAdapter<Object, ViewHolder> {

    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;
    private final Selection<Path, Path> selection;
    private final OnOpenFileListener listener;

    BookmarksAdapter(
            Selection<Path, Path> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback,
            OnOpenFileListener listener) {

        this.listener = requireNonNull(listener);
        this.selection = requireNonNull(selection);
        this.actionModeProvider = requireNonNull(actionModeProvider);
        this.actionModeCallback = requireNonNull(actionModeCallback);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof Path ? 0 : 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return viewType == 0
                ? new BookmarkHolder(inflater.inflate(R.layout.bookmark_item, parent, false))
                : new HeaderHolder(inflater.inflate(R.layout.bookmark_header, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Object item = getItem(position);
        if (holder instanceof BookmarkHolder) {
            ((BookmarkHolder) holder).bind((Path) item, emptyList());
        } else {
            ((HeaderHolder) holder).bind((String) item);
        }
    }

    @Override
    public Object getItemIdObject(int position) {
        return getItem(position);
    }

    private static class HeaderHolder extends RecyclerView.ViewHolder {

        private final TextView headerView;

        HeaderHolder(View itemView) {
            super(itemView);
            this.headerView = find(R.id.header, itemView);
        }

        void bind(String header) {
            headerView.setText(header);
        }
    }

    class BookmarkHolder extends SelectionModeViewHolder<Path, Path> {

        private final TextView titleView;
        private final ImageView iconView;

        BookmarkHolder(View itemView) {
            super(itemView, selection, actionModeProvider, actionModeCallback);
            this.titleView = find(R.id.title, itemView);
            this.iconView = find(R.id.icon, itemView);
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
        protected void onClick(View v, Path item) {
            listener.onOpen(item);
        }
    }
}
