package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.util.ArrayMap;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.selection.SelectionModeViewHolder;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.widget.StableAdapter;
import l.files.ui.browser.action.Selectable;
import l.files.ui.browser.text.FileTextLayouts;
import l.files.ui.browser.widget.FileView;
import l.files.ui.preview.Decode;
import l.files.ui.preview.Preview;
import l.files.ui.preview.Preview.Using;
import l.files.ui.preview.Rect;

import static android.graphics.Color.WHITE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.android.gms.ads.AdRequest.DEVICE_ID_EMULATOR;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.base.view.Views.find;

final class FilesAdapter extends StableAdapter<Object, ViewHolder>
        implements Selectable {

    static final int VIEW_TYPE_FILE = 0;
    static final int VIEW_TYPE_HEADER = 1;
    static final int VIEW_TYPE_AD = 2;

    private final RecyclerView recyclerView;
    private final Preview decorator;

    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;
    private final Selection<Path, FileInfo> selection;

    private final OnOpenFileListener listener;

    private final FileTextLayouts layouts;

    private Rect constraint;
    private int textWidth;

    FilesAdapter(
            RecyclerView recyclerView,
            Selection<Path, FileInfo> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback,
            OnOpenFileListener listener) {

        this.actionModeProvider = requireNonNull(actionModeProvider);
        this.actionModeCallback = requireNonNull(actionModeCallback);
        this.listener = requireNonNull(listener);
        this.selection = requireNonNull(selection);
        this.decorator = Preview.get(recyclerView.getContext());
        this.layouts = FileTextLayouts.get();
        this.recyclerView = requireNonNull(recyclerView);
        this.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView view, int newState) {
                super.onScrollStateChanged(view, newState);

                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    return;
                }

                for (int i = 0; i < view.getChildCount(); i++) {
                    Object tag = view.getChildAt(i).getTag();
                    if (tag instanceof FileHolder) {
                        ((FileHolder) tag).executePendingUpdate();
                    }
                }
            }
        });
    }

    private Rect calculateThumbnailConstraint(Context context, CardView card) {
        Resources res = context.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        int columns = res.getInteger(R.integer.files_grid_columns);
        float cardSpace = SDK_INT >= LOLLIPOP
                ? 0
                : card.getPaddingLeft() + card.getPaddingRight();
        int maxThumbnailWidth = (int) (
                (metrics.widthPixels - res.getDimension(R.dimen.files_list_space) * 2) / columns
                        - res.getDimension(R.dimen.files_item_space_horizontal) * 2
                        - res.getDimension(R.dimen.files_item_card_inner_space) * 2
                        - cardSpace
        );
        int maxThumbnailHeight = (int) (metrics.heightPixels * 1.5);
        return Rect.of(maxThumbnailWidth, maxThumbnailHeight);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof FileInfo) {
            return VIEW_TYPE_FILE;
        } else if (item instanceof Header) {
            return VIEW_TYPE_HEADER;
        } else if (item instanceof Ad) {
            return VIEW_TYPE_AD;
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
                return new FileHolder(inflater.inflate(R.layout.files_grid_item, parent, false));
            case VIEW_TYPE_HEADER:
                return new HeaderHolder(inflater.inflate(R.layout.files_grid_header, parent, false));
            case VIEW_TYPE_AD:
                return new AdHolder(inflater.inflate(R.layout.files_grid_ad, parent, false));
            default:
                throw new IllegalArgumentException(String.valueOf(viewType));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Object item = getItem(position);
        if (item instanceof Header) {
            ((HeaderHolder) holder).bind((Header) item);
        } else if (item instanceof FileInfo) {
            ((FileHolder) holder).bind((FileInfo) item);
        } else if (item instanceof Ad) {
            ((AdHolder) holder).bind();
        } else {
            throw new IllegalArgumentException(String.valueOf(item));
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

        private final View blur;
        private final FileView content;

        private Decode task;

        private boolean pendingUpdateTask;
        private Rect pendingUpdateSize;
        private WeakReference<Bitmap> pendingUpdatePreview;
        private WeakReference<Bitmap> pendingUpdateBlurredThumbnail;

        FileHolder(View itemView) {
            super(itemView, selection, actionModeProvider, actionModeCallback);
            this.content = find(android.R.id.content, this);
            this.blur = find(R.id.blur, this);
            this.itemView.setOnClickListener(this);
            this.itemView.setOnLongClickListener(this);
            this.itemView.setTag(this);
        }

        @Override
        protected Path itemId(FileInfo file) {
            return file.selfPath();
        }

        @Override
        protected void onClick(View v, FileInfo file) {
            listener.onOpen(file.selfPath(), file.linkTargetOrSelfStat());
        }

        void executePendingUpdate() {

            if (pendingUpdateTask) {
                Path file = previewPath();
                Stat stat = previewStat();
                task = decorator.get(file, stat, constraint, this, Using.FILE_EXTENSION);
            }

            if (pendingUpdateBlurredThumbnail != null) {
                Bitmap bitmap = pendingUpdateBlurredThumbnail.get();
                if (bitmap != null) {
                    backgroundBlurFadeIn(bitmap);
                }
            }

            if (pendingUpdatePreview != null) {
                Bitmap bitmap = pendingUpdatePreview.get();
                if (bitmap != null) {
                    content.set(layouts, item(), textWidth, bitmap);
                    content.startPreviewTransition();
                }
            } else if (pendingUpdateSize != null) {
                updateContent(scaleSize(pendingUpdateSize));
            }

            clearPendingUpdates();
        }

        private void clearPendingUpdates() {
            pendingUpdateTask = false;
            pendingUpdateSize = null;
            pendingUpdatePreview = null;
            pendingUpdateBlurredThumbnail = null;
        }

        @Override
        public void bind(FileInfo file) {
            super.bind(file);

            clearPendingUpdates();

            if (constraint == null) {
                constraint = calculateThumbnailConstraint(context(), (CardView) itemView);
                textWidth = constraint.width() - content.getPaddingLeft() - content.getPaddingRight();
            }

            ((CardView) itemView).setCardBackgroundColor(WHITE);
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
        }

        private Object retrievePreview(Using using) {

            if (task != null) {
                task.cancelAll();
                task = null;
            }

            Path file = previewPath();
            Stat stat = previewStat();
            // TODO revisit this if new decoder is added for new file type, existing files will still be marked as not previewable
            // if (stat == null || !decorator.isPreviewable(file, stat, constraint)) {
            if (stat == null) {
                backgroundBlurClear();
                return null;
            }

            Bitmap blurred = decorator.getBlurredThumbnail(file, stat, constraint, false);
            backgroundBlurSet(blurred);

            Bitmap thumbnail = getCachedThumbnail(file, stat);
            if (thumbnail != null) {
                if (blurred == null) {
                    // TODO
                }
                return thumbnail;
            }

            if (canInterruptScrollState()) {
                task = decorator.get(file, stat, constraint, this, using);
            } else {
                pendingUpdateTask = true;
            }

            Rect size = decorator.getSize(file, stat, constraint, false);
            if (size != null) {
                return scaleSize(size);
            }

            return null;
        }

        private boolean canInterruptScrollState() {
            /*
             * SCROLL_STATE_IDLE: view not being scrolled
             * SCROLL_STATE_DRAGGING: finger touching screen, dragging
             * SCROLL_STATE_SETTLING: finger no longer touching screen, view scrolling
             *
             * Don't perform anything expensive like decoding thumbnails
             * in background during SCROLL_STATE_SETTLING as that will interrupt
             * the scrolling animation making the app janky and unresponsive,
             * especially on older device.
             *
             * Doing work in background during SCROLL_STATE_DRAGGING is okay as
             * there is only so much screen space you can drag at once, and user
             * dragging speed is relatively slow. Updating thumbnails during dragging
             * is actually wanted otherwise user will have to lift finger to see
             * things updated which is annoying.
             */
            return recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE ||
                    recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING;
        }

        private Path previewPath() {
            return item().linkTargetOrSelfPath();
        }

        private Stat previewStat() {
            return item().linkTargetOrSelfStat();
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
            return size.scale(constraint);
        }

        private void backgroundBlurClear() {
            blur.setBackground(null);
        }

        private void backgroundBlurSet(Bitmap bitmap) {
            Resources res = itemView.getResources();
            RoundedBitmapDrawable drawable =
                    RoundedBitmapDrawableFactory.create(res, bitmap);
            drawable.setAlpha((int) (0.5f * 255));
            drawable.setCornerRadius(res.getDimension(
                    R.dimen.files_item_card_inner_radius));
            blur.setBackground(drawable);
        }

        private void backgroundBlurFadeIn(Bitmap thumbnail) {
            backgroundBlurSet(thumbnail);
            blur.setAlpha(0f);
            blur.animate()
                    .alpha(1)
                    .setDuration(itemView.getResources().getInteger(
                            android.R.integer.config_longAnimTime));
        }

        @Override
        public void onSizeAvailable(Path item, Stat stat, Rect size) {
            if (item.equals(previewPath())) {
                Rect scaledSize = scaleSize(size);
                if (canInterruptScrollState()) {
                    updateContent(scaledSize);
                } else {
                    pendingUpdateSize = scaledSize;
                }
            }
        }

        @Override
        public void onPreviewAvailable(Path item, Stat stat, Bitmap bm) {
            if (item.equals(previewPath())) {
                if (canInterruptScrollState()) {
                    updateContent(bm);
                    content.startPreviewTransition();
                } else {
                    pendingUpdatePreview = new WeakReference<>(bm);
                }
            }
        }

        @Override
        public void onBlurredThumbnailAvailable(Path path, Stat stat, Bitmap thumbnail) {
            if (path.equals(previewPath())) {
                if (canInterruptScrollState()) {
                    backgroundBlurFadeIn(thumbnail);
                } else {
                    pendingUpdateBlurredThumbnail = new WeakReference<>(thumbnail);
                }
            }
        }

        @Override
        public void onPreviewFailed(Path item, Stat stat, Using used) {
            if (item.equals(previewPath())) {
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

    static final class AdHolder extends ViewHolder {

        private static final AtomicBoolean init = new AtomicBoolean();

        private final NativeExpressAdView adView;
        private boolean adLoaded;

        AdHolder(View itemView) {
            super(itemView);

            // TODO need to call destroy/pause on ad view?
            Context context = itemView.getContext();
            adView = new NativeExpressAdView(context);
            adView.setAdUnitId(getAdUnitId(context));
            adView.setAdSize(calculateAdSize(itemView));
            ((ViewGroup) itemView).addView(adView);

            if (init.compareAndSet(false, true)) {
                MobileAds.initialize(
                        context.getApplicationContext(),
                        itemView.getResources().getString(R.string.ad_app_id));
            }
        }

        private static String getAdUnitId(Context context) {
            return context.getString(R.string.ad_unit_browser_express_id);
        }

        private static AdSize calculateAdSize(View itemView) {
            // width: 280dp - 1200dp, height: 80dp - 612dp
            Resources res = itemView.getResources();
            DisplayMetrics metrics = res.getDisplayMetrics();
            int pad = res.getDimensionPixelSize(R.dimen.files_item_space_horizontal);
            int widthDp = (int) ((metrics.widthPixels - pad * 2) / metrics.density);
            return new AdSize(widthDp, 80);
        }

        void bind() {
            if (adLoaded) {
                return;
            }
            adLoaded = true;

            adView.loadAd(new AdRequest.Builder()
                    .addTestDevice(DEVICE_ID_EMULATOR)
                    .addTestDevice("3D33A77247CFB6111C37C7D2B50E325A") // Nexus 5X
                    .build());

            LayoutParams params = itemView.getLayoutParams();
            if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
            }
        }

    }

}
