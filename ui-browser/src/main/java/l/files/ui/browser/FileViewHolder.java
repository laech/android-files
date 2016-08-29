package l.files.ui.browser;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import javax.annotation.Nullable;

import l.files.base.Provider;
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
import l.files.ui.browser.text.FileTextLayouts;
import l.files.ui.browser.widget.FileView;
import l.files.ui.preview.Decode;
import l.files.ui.preview.Preview;

import static android.graphics.Color.WHITE;
import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.base.content.Contexts.isDebugBuild;
import static l.files.ui.base.view.Views.find;
import static l.files.ui.browser.FilesAdapter.calculateCardContentWidthPixels;

final class FileViewHolder extends SelectionModeViewHolder<Path, FileInfo>
        implements Preview.Callback, LifeCycleListener {

    static final int LAYOUT_ID = R.layout.files_grid_item;

    private final Preview decorator;
    private final OnOpenFileListener listener;
    private final FileTextLayouts layouts;

    private final View blur;
    private final FileView content;
    private final RecyclerView recyclerView;

    @Nullable
    private Rect constraint;
    private int textWidth;

    @Nullable
    private Decode task;

    FileViewHolder(
            View itemView,
            RecyclerView recyclerView,
            LifeCycleListenable listenable,
            Selection<Path, FileInfo> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback,
            OnOpenFileListener listener) {

        super(itemView, selection, actionModeProvider, actionModeCallback);

        this.recyclerView = requireNonNull(recyclerView, "recyclerView");
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

    // TODO support partial update
    @Override
    public void bind(FileInfo file) {
        super.bind(file);

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

    private void updateContent(@Nullable Object preview) {
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

        final Path file = previewPath();
        final Stat stat = previewStat();
        assert constraint != null;
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

        runWhenUiIsIdle(canInterruptScrollState, new Runnable() {
            @Override
            public void run() {
                task = decorator.get(file, stat, constraint, FileViewHolder.this);
            }
        });

        Rect size = decorator.getSize(file, stat, constraint, false);
        if (size != null) {
            return scaleSize(size);
        }

        return null;
    }

    private final Provider<Boolean> canUpdate = new Provider<Boolean>() {
        @Override
        public Boolean get() {
            ItemAnimator animator = recyclerView.getItemAnimator();
            return (animator == null || !animator.isRunning()) &&
                    canInterruptScrollState.get();
        }
    };

    private final Provider<Boolean> canInterruptScrollState = new Provider<Boolean>() {
        @Override
        public Boolean get() {
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
            int scrollState = recyclerView.getScrollState();
            return scrollState == SCROLL_STATE_IDLE ||
                    scrollState == SCROLL_STATE_DRAGGING;
        }
    };

    private Path previewPath() {
        return item().linkTargetOrSelfPath();
    }

    @Nullable
    private Stat previewStat() {
        return item().linkTargetOrSelfStat();
    }

    @Nullable
    private Bitmap getCachedThumbnail(Path path, Stat stat) {
        long now = currentTimeMillis();
        long then = stat.lastModifiedTime().to(MILLISECONDS);
        boolean changedMoreThan5SecondsAgo = now - then > 5000;
        assert constraint != null;
        if (changedMoreThan5SecondsAgo) {
            return decorator.getThumbnail(path, stat, constraint, true);
        } else {
            return decorator.getThumbnail(path, stat, constraint, false);
        }
    }

    private Rect scaleSize(Rect size) {
        assert constraint != null;
        return size.scaleDown(constraint);
    }

    private void backgroundBlurClear() {
        blur.setBackground(null);
    }

    private void backgroundBlurSet(@Nullable Bitmap bitmap) {
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
        runWhenUiIsIdle(canUpdate, new Runnable() {
            @Override
            public void run() {
                int position = getAdapterPosition();
                if (position != NO_POSITION) {
                    recyclerView.getAdapter().notifyItemChanged(position);
                }
            }
        });
    }

    @Override
    public void onBlurredThumbnailAvailable(
            final Path path,
            final Stat stat,
            final Bitmap thumbnail) {
        runWhenUiIsIdle(canUpdate, new Runnable() {
            @Override
            public void run() {
                // TODO can remove fade in when notifyItemChanged(position, playload) is supported
                backgroundBlurFadeIn(thumbnail);
            }
        });
    }

    private void runWhenUiIsIdle(
            final Provider<Boolean> canUpdate,
            final Runnable update) {
        final FileInfo oldItem = item();
        new Runnable() {
            @Override
            public void run() {
                FileInfo newItem = item();
                if (!oldItem.equals(newItem)) {
                    return;
                }
                if (!canUpdate.get()) {
                    itemView.postDelayed(this, 50);
                    return;
                }
                int position = getAdapterPosition();
                if (position != NO_POSITION) {
                    update.run();
                }
            }
        }.run();
    }

    @Override
    public void onPreviewFailed(Path item, Stat stat, Object cause) {

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
