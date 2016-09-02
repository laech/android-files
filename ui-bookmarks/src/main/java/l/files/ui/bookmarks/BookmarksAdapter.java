package l.files.ui.bookmarks;

import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import l.files.fs.Path;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.selection.SelectionModeViewHolder;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.widget.StableAdapter;
import l.files.ui.bookmarks.databinding.BookmarkHeaderBinding;
import l.files.ui.bookmarks.databinding.BookmarkItemBinding;

import static l.files.base.Objects.requireNonNull;

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
                ? new BookmarkHolder(BookmarkItemBinding.inflate(inflater, parent, false))
                : new HeaderHolder(BookmarkHeaderBinding.inflate(inflater, parent, false));
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

        private final BookmarkHeaderBinding binding;

        HeaderHolder(BookmarkHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String header) {
            binding.setHeader(header);
        }
    }

    class BookmarkHolder extends SelectionModeViewHolder<Path, Path> {

        private final BookmarkItemBinding binding;

        BookmarkHolder(BookmarkItemBinding binding) {
            super(binding.getRoot(), selection, actionModeProvider, actionModeCallback);
            this.binding = binding;
        }

        @Override
        protected Path itemId(Path file) {
            return file;
        }

        @Override
        public void bind(Path path) {
            super.bind(path);
            binding.setPath(path);
        }

        @Override
        protected void onClick(View v, Path item) {
            listener.onOpen(item);
        }
    }
}
