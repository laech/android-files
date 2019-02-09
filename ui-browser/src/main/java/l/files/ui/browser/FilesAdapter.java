package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import androidx.appcompat.view.ActionMode;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import kotlin.sequences.Sequence;
import l.files.fs.Path;
import l.files.ui.base.app.LifeCycleListenable;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.fs.OpenFileEvent;
import l.files.ui.base.messaging.MainThreadTopic;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.widget.StableAdapter;
import l.files.ui.browser.action.Selectable;

import static java.util.Collections.emptyList;
import static kotlin.collections.CollectionsKt.asSequence;
import static kotlin.sequences.SequencesKt.associateBy;
import static kotlin.sequences.SequencesKt.filterIsInstance;
import static l.files.base.Objects.requireNonNull;

final class FilesAdapter extends StableAdapter<Object, ViewHolder> implements Selectable {

    static final int VIEW_TYPE_FILE = 0;
    static final int VIEW_TYPE_HEADER = 1;

    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;
    private final Selection<Path, FileInfo> selection;

    private final MainThreadTopic<OpenFileEvent> topic;

    private final LifeCycleListenable listenable;
    private final RecyclerView recyclerView;

    FilesAdapter(
            RecyclerView recyclerView,
            LifeCycleListenable listenable,
            Selection<Path, FileInfo> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback,
            MainThreadTopic<OpenFileEvent> topic
    ) {
        this.recyclerView = requireNonNull(recyclerView);
        this.listenable = requireNonNull(listenable);
        this.actionModeProvider = requireNonNull(actionModeProvider);
        this.actionModeCallback = requireNonNull(actionModeCallback);
        this.topic = requireNonNull(topic);
        this.selection = requireNonNull(selection);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof FileInfo) return VIEW_TYPE_FILE;
        if (item instanceof Header) return VIEW_TYPE_HEADER;
        throw new IllegalArgumentException(String.valueOf(item));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_FILE) return newFileViewHolder(inflater, parent);
        if (viewType == VIEW_TYPE_HEADER) return newHeaderViewHolder(inflater, parent);
        throw new IllegalArgumentException(String.valueOf(viewType));
    }

    private FileViewHolder newFileViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new FileViewHolder(
                inflater.inflate(FileViewHolder.LAYOUT_ID, parent, false),
                recyclerView,
                listenable,
                selection,
                actionModeProvider,
                actionModeCallback,
                topic
        );
    }

    private ViewHolder newHeaderViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new HeaderViewHolder(inflater.inflate(HeaderViewHolder.LAYOUT_ID, parent, false));
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
    public Object getItemIdObject(Object item) {
        if (item instanceof FileInfo) {
            return ((FileInfo) item).selfPath();
        }
        return item;
    }

    @Override
    public void selectAll() {
        Sequence<FileInfo> files = filterIsInstance(asSequence(items()), FileInfo.class);
        selection.addAll(associateBy(files, FileInfo::selfPath));
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
