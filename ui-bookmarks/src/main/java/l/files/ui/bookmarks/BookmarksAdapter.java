package l.files.ui.bookmarks;

import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import l.files.fs.Path;
import l.files.ui.base.fs.FileLabels;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.selection.SelectionModeViewHolder;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.widget.StableAdapter;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.base.fs.FileIcons.directoryIconStringId;

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
            ((BookmarkHolder) holder).bind((Path) item);
        } else {
            ((HeaderHolder) holder).bind((String) item);
        }
    }

    @Override
    public Object getItemIdObject(int position) {
        return getItem(position);
    }

    private static class HeaderHolder extends RecyclerView.ViewHolder {

        HeaderHolder(View itemView) {
            super(itemView);
        }

        void bind(String header) {
            ((TextView) itemView).setText(header);
        }
    }

    class BookmarkHolder extends SelectionModeViewHolder<Path, Path> {

        BookmarkHolder(View itemView) {
            super(itemView, selection, actionModeProvider, actionModeCallback);
        }

        @Override
        protected Path itemId(Path file) {
            return file;
        }

        @Override
        public void bind(Path file) {
            super.bind(file);
            String icon = context().getString(directoryIconStringId(file));
            String title = FileLabels.get(resources(), file);
            ((BookmarkView) itemView).set(icon, title);
        }

        @Override
        protected void onClick(View v, Path item) {
            listener.onOpen(item);
        }
    }
}
