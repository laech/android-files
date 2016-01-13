package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.util.ArrayMap;
import android.support.v7.graphics.Palette;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.selection.SelectionModeViewHolder;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.widget.StableAdapter;
import l.files.ui.preview.Decode;
import l.files.ui.preview.Preview;
import l.files.ui.preview.Preview.Using;
import l.files.ui.preview.Rect;

import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Color.WHITE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.base.view.Views.find;
import static l.files.ui.browser.R.dimen.files_item_card_inner_space;
import static l.files.ui.browser.R.dimen.files_item_space_horizontal;
import static l.files.ui.browser.R.dimen.files_list_space;
import static l.files.ui.browser.R.integer.files_grid_columns;
import static l.files.ui.preview.Preview.darkColor;

final class FilesAdapter extends StableAdapter<Object, ViewHolder>
        implements Selectable {

    static final int VIEW_TYPE_FILE = 0;
    static final int VIEW_TYPE_HEADER = 1;

    private final Context context;
    private final Preview decorator;

    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;
    private final Selection<Path, FileInfo> selection;

    private final OnOpenFileListener listener;

    private final FileTextLayoutCache layouts;

    private Rect constraint;
    private int textWidth;

    FilesAdapter(
            Context context,
            Selection<Path, FileInfo> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback,
            OnOpenFileListener listener) {

        this.context = requireNonNull(context);
        this.actionModeProvider = requireNonNull(actionModeProvider);
        this.actionModeCallback = requireNonNull(actionModeCallback);
        this.listener = requireNonNull(listener);
        this.selection = requireNonNull(selection);
        this.decorator = Preview.get(context);
        this.layouts = FileTextLayoutCache.get();

    }

    private Rect calculateThumbnailConstraint(Context context, CardView card) {
        Resources res = context.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        int columns = res.getInteger(files_grid_columns);
        float cardSpace = SDK_INT >= LOLLIPOP
                ? 0
                : card.getPaddingLeft() + card.getPaddingRight();
        int maxThumbnailWidth = (int) (
                (metrics.widthPixels - res.getDimension(files_list_space) * 2) / columns
                        - res.getDimension(files_item_space_horizontal) * 2
                        - res.getDimension(files_item_card_inner_space) * 2
                        - cardSpace
        );
        int maxThumbnailHeight = (int) (metrics.heightPixels * 1.5);
        return Rect.of(maxThumbnailWidth, maxThumbnailHeight);
    }

    void warmUpOnIdle(StaggeredGridLayoutManager layout) {

        System.gc();

        int[] lastVisiblePositions;
        try {
            lastVisiblePositions = layout.findLastVisibleItemPositions(null);
        } catch (RuntimeException e) {
            e.printStackTrace();
            /*
             * java.lang.NullPointerException: Attempt to invoke virtual method 'int android.support.v7.widget.OrientationHelper.getStartAfterPadding()' on a null object reference
             *     at android.support.v7.widget.StaggeredGridLayoutManager$Span.findOneVisibleChild(StaggeredGridLayoutManager.java:2345)
             *     at android.support.v7.widget.StaggeredGridLayoutManager$Span.findLastVisibleItemPosition(StaggeredGridLayoutManager.java:2333)
             *     at android.support.v7.widget.StaggeredGridLayoutManager.findLastVisibleItemPositions(StaggeredGridLayoutManager.java:897)
             */
            return;
        }
        Arrays.sort(lastVisiblePositions);
        int pos = lastVisiblePositions[lastVisiblePositions.length - 1];
        if (pos == NO_POSITION) {
            return;
        }

        int warmUpToPosition = pos + 50;

        while (pos <= warmUpToPosition && pos < getItemCount()) {
            Object item = getItem(pos);
            if (item instanceof FileInfo) {
                layouts.getName(context, (FileInfo) item, textWidth);
                layouts.getLink(context, (FileInfo) item, textWidth);
                layouts.getSummary(context, (FileInfo) item, textWidth);
            }
            pos++;
        }

    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof FileInfo
                ? VIEW_TYPE_FILE
                : VIEW_TYPE_HEADER;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        return viewType == VIEW_TYPE_FILE
                ? new FileHolder(inflater.inflate(R.layout.files_grid_item, parent, false))
                : new HeaderHolder(inflater.inflate(R.layout.files_grid_header, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Object item = getItem(position);
        if (item instanceof Header) {
            ((HeaderHolder) holder).bind((Header) item);
        } else {
            ((FileHolder) holder).bind((FileInfo) item);
        }
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

    final class FileHolder extends SelectionModeViewHolder<Path, FileInfo>
            implements Preview.Callback {

        private final FileView content;

        private final float itemViewElevationWithPreview;
        private final float itemViewElevationWithoutPreview;

        private Decode task;

        FileHolder(View itemView) {
            super(itemView, selection, actionModeProvider, actionModeCallback);
            this.content = find(android.R.id.content, this);
            this.itemView.setOnClickListener(this);
            this.itemView.setOnLongClickListener(this);
            this.itemViewElevationWithoutPreview = dimen(R.dimen.files_item_elevation_without_preview);
            this.itemViewElevationWithPreview = dimen(R.dimen.files_item_elevation_with_preview);
        }

        private float dimen(int id) {
            return resources().getDimension(id);
        }

        @Override
        protected Path itemId(FileInfo file) {
            return file.selfPath();
        }

        @Override
        protected void onClick(View v, FileInfo file) {
            listener.onOpen(file.selfPath(), file.linkTargetOrSelfStat());
        }

        @Override
        public void bind(FileInfo file) {
            super.bind(file);
            if (constraint == null) {
                constraint = calculateThumbnailConstraint(context(), (CardView) itemView);
                textWidth = constraint.width() - content.getPaddingStart() - content.getPaddingEnd();
            }

            updateContent(retrievePreview(Using.FILE_EXTENSION));
        }

        private void updateContent(Object preview) {
            if (preview instanceof Rect) {
                content.set(layouts, item(), textWidth, (Rect) preview);
            } else if (preview instanceof Bitmap) {
                content.set(layouts, item(), textWidth, (Bitmap) preview);
            } else {
                content.set(layouts, item(), textWidth, (Bitmap) null);
            }
            content.setEnabled(item().isReadable());
            ((CardView) itemView).setCardElevation(
                    preview != null
                            ? itemViewElevationWithPreview
                            : itemViewElevationWithoutPreview);
        }

        private Object retrievePreview(Using using) {

            if (task != null) {
                task.cancelAll();
            }

            Path file = previewFile();
            Stat stat = item().linkTargetOrSelfStat();
            if (stat == null || !decorator.isPreviewable(file, stat, constraint)) {
                setPaletteColor(TRANSPARENT);
                return null;
            }

            Palette palette = decorator.getPalette(file, stat, constraint, false);
            if (palette != null) {
                setPaletteColor(backgroundColor(palette));
            } else {
                setPaletteColor(TRANSPARENT);
            }

            Bitmap thumbnail = getCachedThumbnail(file, stat);
            if (thumbnail != null) {
                return thumbnail;
            }

            task = decorator.get(file, stat, constraint, this, using);

            Rect size = decorator.getSize(file, stat, constraint, false);
            if (size != null) {
                return scaleSize(size);
            }

            return null;
        }

        private Path previewFile() {
            return item().linkTargetOrSelfPath();
        }

        private Bitmap getCachedThumbnail(Path path, Stat stat) {
            long now = currentTimeMillis();
            long then = stat.lastModifiedTime().to(MILLISECONDS);
            boolean changedMoreThan5SecondsAgo = now - then > 5000;
            if (changedMoreThan5SecondsAgo) {
                return decorator.getThumbnail(path, stat, constraint, true);
            } else {
                return decorator.getThumbnail(path, stat, constraint, false);
            }
        }

        private Rect scaleSize(Rect size) {

            // TODO scale up too for small pics to avoid jumping

            boolean tooBig = size.width() > constraint.width()
                    || size.height() > constraint.height();

            return tooBig
                    ? size.scale(constraint)
                    : size;

        }

        private void setPaletteColor(int color) {
            if (color == TRANSPARENT) {
                ((CardView) itemView).setCardBackgroundColor(WHITE);
                content.setUseInverseTextColor(false);
            } else {
                ((CardView) itemView).setCardBackgroundColor(color);
                content.setUseInverseTextColor(true);
            }
        }

        @Override
        public void onSizeAvailable(Path item, Stat stat, Rect size) {
            if (item.equals(previewFile())) {
                updateContent(scaleSize(size));
            }
        }

        @Override
        public void onPaletteAvailable(Path item, Stat stat, Palette palette) {
            if (item.equals(previewFile())) {
                setPaletteColor(backgroundColor(palette));
            }
        }

        private int backgroundColor(Palette palette) {
            return darkColor(palette, TRANSPARENT);
        }

        @Override
        public void onPreviewAvailable(Path item, Stat stat, Bitmap bm) {
            if (item.equals(previewFile())) {
                updateContent(bm);
                content.startPreviewTransition();
            }
        }

        @Override
        public void onPreviewFailed(Path item, Stat stat, Using used) {
            if (item.equals(previewFile())) {
                updateContent(null);
            }
        }
    }

    final class HeaderHolder extends ViewHolder {
        private TextView title;

        HeaderHolder(View itemView) {
            super(itemView);
            title = find(android.R.id.title, this);
        }

        void bind(Header header) {
            title.setText(header.toString());
            LayoutParams params = itemView.getLayoutParams();
            if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
            }
        }
    }

}
