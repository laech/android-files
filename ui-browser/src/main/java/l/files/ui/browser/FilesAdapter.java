package l.files.ui.browser;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import l.files.fs.File;
import l.files.fs.Stat;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.selection.SelectionModeViewHolder;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.widget.StableAdapter;
import l.files.ui.browser.BrowserItem.FileItem;
import l.files.ui.browser.BrowserItem.HeaderItem;
import l.files.ui.preview.Decode;
import l.files.ui.preview.Preview;
import l.files.ui.preview.PreviewCallback;
import l.files.ui.preview.Rect;
import l.files.ui.preview.SizedColorDrawable;

import static android.R.attr.textColorPrimary;
import static android.R.attr.textColorPrimaryInverse;
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
import static l.files.ui.browser.R.layout.files_grid_header;
import static l.files.ui.browser.R.layout.files_grid_item;
import static l.files.ui.browser.Styles.getColorStateList;

final class FilesAdapter extends StableAdapter<BrowserItem, ViewHolder>
        implements Selectable {

    static final int VIEW_TYPE_FILE = 0;
    static final int VIEW_TYPE_HEADER = 1;

    private final Context context;
    private final Preview decorator;
    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;
    private final Selection<File> selection;
    private final OnOpenFileListener listener;
    private Rect constraint;
    private int textWidth;

    FilesAdapter(
            Context context,
            Selection<File> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback,
            OnOpenFileListener listener) {

        this.context = requireNonNull(context);
        this.actionModeProvider = requireNonNull(actionModeProvider);
        this.actionModeCallback = requireNonNull(actionModeCallback);
        this.listener = requireNonNull(listener);
        this.selection = requireNonNull(selection);
        this.decorator = Preview.get(context);
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
            BrowserItem item = getItem(pos);
            if (item instanceof FileItem) {
                FileTextLayoutCache.get(context, (FileItem) item, textWidth);
            }
            pos++;
        }

        FileTextLayoutCache.printStat();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isFileItem() ? VIEW_TYPE_FILE : VIEW_TYPE_HEADER;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        return viewType == VIEW_TYPE_FILE
                ? new FileHolder(inflater.inflate(files_grid_item, parent, false))
                : new HeaderHolder(inflater.inflate(files_grid_header, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BrowserItem item = getItem(position);
        if (item.isHeaderItem()) {
            ((HeaderHolder) holder).bind((HeaderItem) item);
        } else {
            ((FileHolder) holder).bind((FileItem) item);
        }
    }

    @Override
    public Object getItemIdObject(int position) {
        BrowserItem item = getItem(position);
        if (item instanceof FileItem) {
            return ((FileItem) item).selfFile();
        }
        return item;
    }

    @Override
    public void selectAll() {
        List<BrowserItem> items = items();
        List<File> files = new ArrayList<>(items.size());
        for (BrowserItem item : items) {
            if (item.isFileItem()) {
                files.add(((FileItem) item).selfFile());
            }
        }
        selection.addAll(files);
    }

    final class FileHolder extends SelectionModeViewHolder<File, FileItem>
            implements PreviewCallback {

        private final FileView content;

        private final float previewRadius;
        private final int transitionDuration;

        private final ColorStateList primaryText;
        private final ColorStateList primaryTextInverse;

        private Decode task;

        FileHolder(View itemView) {
            super(itemView, selection, actionModeProvider, actionModeCallback);
            this.content = find(android.R.id.content, this);
            this.itemView.setOnClickListener(this);
            this.itemView.setOnLongClickListener(this);
            this.previewRadius = itemView.getResources().getDimension(R.dimen.files_item_card_inner_radius);
            this.transitionDuration = resources().getInteger(android.R.integer.config_shortAnimTime);

            this.primaryText = getColorStateList(textColorPrimary, context());
            this.primaryTextInverse = getColorStateList(textColorPrimaryInverse, context());
        }

        @Override
        protected File itemId(FileItem file) {
            return file.selfFile();
        }

        @Override
        protected void onClick(View v, FileItem file) {
            listener.onOpen(file.selfFile(), file.linkTargetOrSelfStat());
        }

        @Override
        public void bind(FileItem file) {
            super.bind(file);
            if (constraint == null) {
                constraint = calculateThumbnailConstraint(context(), (CardView) itemView);
                textWidth = constraint.width() - content.getPaddingStart() - content.getPaddingEnd();
            }

            updateContent(retrievePreview());
        }

        private void updateContent(Drawable preview) {
            content.set(item(), textWidth, preview);
            content.setEnabled(item().isReadable());
        }

        private Drawable retrievePreview() {

            if (task != null) {
                task.cancelAll();
            }

            File res = item().selfFile();
            Stat stat = item().linkTargetOrSelfStat();
            if (stat == null || !decorator.isPreviewable(res, stat, constraint)) {
                setPaletteColor(TRANSPARENT); // TODO
                return null;
            }

            Palette palette = decorator.getPalette(res, null, constraint);
            if (palette != null) {
                setPaletteColor(backgroundColor(palette));
            } else {
                setPaletteColor(TRANSPARENT);
            }

            Bitmap thumbnail = getCachedThumbnail(res, stat);
            if (thumbnail != null) {
                return newThumbnailDrawable(thumbnail);
            }

            task = decorator.get(res, stat, constraint, this);

            Rect size = decorator.getSize(res, null, constraint);
            if (size != null) {
                return newSizedColorDrawable(size);
            } else {
                return null;
            }

        }

        private Bitmap getCachedThumbnail(File res, Stat stat) {
            long now = currentTimeMillis();
            long then = stat.lastModifiedTime().to(MILLISECONDS);
            boolean changedMoreThan5SecondsAgo = now - then > 5000;
            if (changedMoreThan5SecondsAgo) {
                return decorator.getThumbnail(res, stat, constraint);
            } else {
                return decorator.getThumbnail(res, null, constraint);
            }
        }

        private SizedColorDrawable newSizedColorDrawable(Rect size) {

            boolean tooBig = size.width() > constraint.width()
                    || size.height() > constraint.height();

            Rect scaledDown = tooBig
                    ? size.scale(constraint)
                    : size;

            return new SizedColorDrawable(TRANSPARENT, scaledDown);

        }

        private void setPaletteColor(int color) {
            if (color == TRANSPARENT) {
                ((CardView) itemView).setCardBackgroundColor(WHITE);
                content.setTextColor(primaryText);
            } else {
                ((CardView) itemView).setCardBackgroundColor(color);
                content.setTextColor(primaryTextInverse);
            }
        }

        @Override
        public void onSizeAvailable(File item, Rect size) {
            if (item.equals(itemId())) {
                updateContent(newSizedColorDrawable(size));
            }
        }

        @Override
        public void onPaletteAvailable(File item, Palette palette) {
            if (item.equals(itemId())) {
                setPaletteColor(backgroundColor(palette));
            }
        }

        private int backgroundColor(Palette palette) {
            int color = palette.getDarkVibrantColor(TRANSPARENT);
            if (color == TRANSPARENT) {
                color = palette.getDarkMutedColor(TRANSPARENT);
            }
            return color;
        }

        @Override
        public void onPreviewAvailable(File item, Bitmap bm) {
            if (item.equals(itemId())) {
                TransitionDrawable transition = new TransitionDrawable(new Drawable[]{
                        new SizedColorDrawable(TRANSPARENT, bm.getWidth(), bm.getHeight()),
                        newThumbnailDrawable(bm)
                });
                updateContent(transition);
                transition.startTransition(transitionDuration);
            }
        }

        private Drawable newThumbnailDrawable(Bitmap thumbnail) {
            return new ThumbnailDrawable(context(), previewRadius, thumbnail);
        }

        @Override
        public void onPreviewFailed(File item) {
            if (item.equals(itemId())) {
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

        void bind(HeaderItem header) {
            title.setText(header.toString());
            LayoutParams params = itemView.getLayoutParams();
            if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
            }
        }
    }

}
