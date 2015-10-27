package l.files.ui.bookmarks;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import l.files.fs.File;
import l.files.ui.base.fs.FileIcons;
import l.files.ui.base.fs.FileLabels;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.selection.SelectionModeViewHolder;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.widget.StableAdapter;

import static java.util.Objects.requireNonNull;
import static l.files.ui.base.fs.FileIcons.directoryIconStringId;
import static l.files.ui.base.view.Views.find;

final class BookmarksAdapter extends StableAdapter<Object, ViewHolder> {

    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;
    private final Selection<File> selection;
    private final OnOpenFileListener listener;

    BookmarksAdapter(
            Selection<File> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback,
            OnOpenFileListener listener) {

        this.listener = requireNonNull(listener, "listener");
        this.selection = requireNonNull(selection, "selection");
        this.actionModeProvider = requireNonNull(actionModeProvider, "actionModeProvider");
        this.actionModeCallback = requireNonNull(actionModeCallback, "actionModeCallback");
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof File ? 0 : 1;
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
            ((BookmarkHolder) holder).bind((File) item);
        } else {
            ((HeaderHolder) holder).bind((String) item);
        }
    }

    @Override
    public Object getItemIdObject(int position) {
        return getItem(position);
    }

    private static class HeaderHolder extends RecyclerView.ViewHolder {

        private final TextView title;

        HeaderHolder(View itemView) {
            super(itemView);
            title = find(android.R.id.title, this);
        }

        void bind(String header) {
            title.setText(header);
        }
    }

    class BookmarkHolder extends SelectionModeViewHolder<File, File> {

        private final TextView title;
        private final TextView icon;

        BookmarkHolder(View itemView) {
            super(itemView, selection, actionModeProvider, actionModeCallback);
            title = find(android.R.id.title, this);
            icon = find(android.R.id.icon, this);
            icon.setTypeface(FileIcons.font(itemView.getResources().getAssets()));
        }

        @Override
        protected File itemId(File file) {
            return file;
        }

        @Override
        public void bind(File file) {
            super.bind(file);
            title.setText(FileLabels.get(title.getResources(), file));
            icon.setText(directoryIconStringId(file));
        }

        @Override
        protected void onClick(View v, File item) {
            listener.onOpen(item);
        }
    }
}
