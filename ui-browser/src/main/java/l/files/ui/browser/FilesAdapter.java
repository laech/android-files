package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.util.ArrayMap;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import l.files.fs.Path;
import l.files.ui.base.app.LifeCycleListenable;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.widget.StableAdapter;
import l.files.ui.browser.action.Selectable;

import static java.util.Collections.emptyList;
import static l.files.base.Objects.requireNonNull;

final class FilesAdapter extends StableAdapter<Object, ViewHolder>
        implements Selectable {

    static final int VIEW_TYPE_FILE = 0;
    static final int VIEW_TYPE_HEADER = 1;

    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;
    private final Selection<Path, FileInfo> selection;

    private final OnOpenFileListener listener;

    private final LifeCycleListenable listenable;
    private final RecyclerView recyclerView;

    FilesAdapter(
            RecyclerView recyclerView,
            LifeCycleListenable listenable,
            Selection<Path, FileInfo> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback,
            OnOpenFileListener listener) {

        this.recyclerView = requireNonNull(recyclerView);
        this.listenable = requireNonNull(listenable);
        this.actionModeProvider = requireNonNull(actionModeProvider);
        this.actionModeCallback = requireNonNull(actionModeCallback);
        this.listener = requireNonNull(listener);
        this.selection = requireNonNull(selection);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof FileInfo) {
            return VIEW_TYPE_FILE;
        } else if (item instanceof Header) {
            return VIEW_TYPE_HEADER;
        } else {
            throw new IllegalArgumentException(String.valueOf(item));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {

            case VIEW_TYPE_FILE:
                return new FileViewHolder(
                        inflater.inflate(FileViewHolder.LAYOUT_ID, parent, false),
                        recyclerView,
                        listenable,
                        selection,
                        actionModeProvider,
                        actionModeCallback,
                        listener);

            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(
                        inflater.inflate(HeaderViewHolder.LAYOUT_ID, parent, false));

            default:
                throw new IllegalArgumentException(String.valueOf(viewType));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        Object item = getItem(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((Header) item);
        } else if (holder instanceof FileViewHolder) {
            ((FileViewHolder) holder).bind((FileInfo) item, payloads);
        } else {
            throw new IllegalArgumentException(String.valueOf(item));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        onBindViewHolder(holder, position, emptyList());
    }

    @Override
    public Object getItemIdObject(int position) {
        Object item = getItem(position);
        if (item instanceof FileInfo) {
            return ((FileInfo) item).selfPath();
        }
        return item;
    }

    @Override
    public void selectAll() {
        List<Object> items = items();
        Map<Path, FileInfo> files = new ArrayMap<>(items.size());
        for (Object item : items) {
            if (item instanceof FileInfo) {
                FileInfo file = (FileInfo) item;
                files.put(file.selfPath(), file);
            }
        }
        selection.addAll(files);
    }

    static int calculateCardContentWidthPixels(CardView card, int columns) {
        Resources res = card.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        float padding = card.getPaddingLeft() + card.getPaddingRight();
        return (int) (
                (metrics.widthPixels - res.getDimension(R.dimen.files_list_space) * 2) / columns
                        - res.getDimension(R.dimen.files_item_space_horizontal) * 2
                        - res.getDimension(R.dimen.files_item_card_inner_space) * 2
                        - padding
        );
    }
}
