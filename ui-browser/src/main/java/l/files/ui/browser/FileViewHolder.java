package l.files.ui.browser;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.app.LifeCycleListenable;
import l.files.ui.base.app.LifeCycleListener;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.selection.SelectionModeViewHolder;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.browser.FilesAdapter.ScrollStateListener;
import l.files.ui.browser.text.FileTextLayouts;
import l.files.ui.browser.widget.FileView;
import l.files.ui.preview.Decode;
import l.files.ui.preview.Preview;

import static android.graphics.Color.WHITE;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.base.content.Contexts.isDebugBuild;
import static l.files.ui.base.view.Views.find;
import static l.files.ui.browser.FilesAdapter.calculateCardContentWidthPixels;

final class FileViewHolder extends SelectionModeViewHolder<Path, FileInfo>
        implements Preview.Callback, LifeCycleListener, ScrollStateListener {

    static final int LAYOUT_ID = R.layout.files_grid_item;

    private final Preview decorator;
    private final OnOpenFileListener listener;
    private final FileTextLayouts layouts;

    private final View blur;
    private final FileView content;

    private Rect constraint;
    private int textWidth;

    private Decode task;

    private boolean pendingUpdateTask;
    private Rect pendingUpdateSize;
    private WeakReference<Bitmap> pendingUpdatePreview;
    private WeakReference<Bitmap> pendingUpdateBlurredThumbnail;

    private int scrollState;

    FileViewHolder(
            View itemView,
            LifeCycleListenable listenable,
            Selection<Path, FileInfo> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback,
            OnOpenFileListener listener) {

        super(itemView, selection, actionModeProvider, actionModeCallback);

        this.decorator = Preview.get(itemView.getContext());
        this.listener = requireNonNull(listener, "listener");
        this.layouts = FileTextLayouts.get();
        this.content = find(android.R.id.content, this);
        this.blur = find(R.id.blur, this);
        this.itemView.setOnClickListener(this);
        this.itemView.setOnLongClickListener(this);

        listenable.addWeaklyReferencedLifeCycleListener(this);
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
    public void onCurrentState(int newState) {
        scrollState = newState;
    }

    @Override
    public void onScrollStateChanged(RecyclerView view, int newState) {
        scrollState = newState;
        if (newState == SCROLL_STATE_IDLE) {
            executePendingUpdate();
        }
    }

    private void executePendingUpdate() {

        if (pendingUpdateTask) {
            Path file = previewPath();
            Stat stat = previewStat();
            task = decorator.get(file, stat, constraint, this);
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
            constraint = calculateThumbnailConstraint((CardView) find(R.id.card, this));
            textWidth = constraint.width()
                    - content.getPaddingLeft()
                    - content.getPaddingRight();
        }

        ((CardView) itemView).setCardBackgroundColor(WHITE);
        updateContent(retrievePreview());
    }

    private Rect calculateThumbnailConstraint(CardView card) {
        Resources res = card.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        int columns = res.getInteger(R.integer.files_grid_columns);
        int maxThumbnailWidth = calculateCardContentWidthPixels(card, columns);
        int maxThumbnailHeight = (int) (metrics.heightPixels * 1.5);
        return Rect.of(maxThumbnailWidth, maxThumbnailHeight);
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

    private Object retrievePreview() {

        if (task != null) {
            task.cancelAll();
            task = null;
        }

        Path file = previewPath();
        Stat stat = previewStat();
        if (stat == null || !decorator.isPreviewable(file, stat, constraint)) {
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
            task = decorator.get(file, stat, constraint, this);
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
        return scrollState == SCROLL_STATE_IDLE ||
                scrollState == RecyclerView.SCROLL_STATE_DRAGGING;
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
        return size.scaleDown(constraint);
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
    public void onPreviewFailed(
            Path item, Stat stat, Object cause) {

        if (item.equals(previewPath())) {
            updateContent(null);
        }

        if (isDebugBuild(context())) {
            if (cause instanceof Throwable) {
                Log.d(getClass().getSimpleName(),
                        "No preview " + item, (Throwable) cause);
            } else {
                Log.d(getClass().getSimpleName(),
                        "No preview " + item + " (" + cause + ")");
            }
        }
    }

    @Override
    public void onDestroy() {
        if (task != null) {
            task.cancelAll();
            task = null;
        }
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }
}
