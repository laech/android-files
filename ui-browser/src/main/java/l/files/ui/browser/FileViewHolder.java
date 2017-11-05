package l.files.ui.browser;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

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
import l.files.ui.browser.widget.ActivatedCardView;
import l.files.ui.browser.widget.ActivatedCardView.ActivatedListener;
import l.files.ui.preview.Preview;
import l.files.ui.preview.SizedColorDrawable;

import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Color.WHITE;
import static android.support.v4.content.ContextCompat.getColor;
import static android.support.v4.content.ContextCompat.getDrawable;
import static android.support.v4.graphics.drawable.DrawableCompat.setTintList;
import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.base.content.Contexts.isDebugBuild;
import static l.files.ui.browser.FilesAdapter.calculateCardContentWidthPixels;

public final class FileViewHolder extends SelectionModeViewHolder<Path, FileInfo>
        implements Preview.Callback, LifeCycleListener, ActivatedListener {

    static final int LAYOUT_ID = R.layout.files_grid_item;

    private final Preview decorator;
    private final OnOpenFileListener listener;

    private final ImageView imageView;
    private final TextView titleView;
    private final TextView linkView;
    private final TextView summaryView;
    private final View blurView;
    private final CardView cardView;
    private final RecyclerView recyclerView;

    @Nullable
    private Rect constraint;

    @Nullable
    private AsyncTask<?, ?, ?> task;

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
        this.blurView = itemView.findViewById(R.id.blur);
        this.cardView = itemView.findViewById(R.id.card);
        this.titleView = itemView.findViewById(R.id.title);
        this.linkView = itemView.findViewById(R.id.link);
        this.summaryView = itemView.findViewById(R.id.summary);
        this.imageView = itemView.findViewById(R.id.image);
        this.itemView.setOnClickListener(this);
        this.itemView.setOnLongClickListener(this);

        ((ActivatedCardView) itemView).setActivatedListener(this);

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
    public void bind(FileInfo file, List<Object> payloads) {
        super.bind(file, payloads);

        if (constraint == null) {
            constraint = calculateThumbnailConstraint(cardView);
        }

        if (payloads.isEmpty()) {
            bindFull(file);
        } else {
            bindPartial(payloads);
        }
    }

    private void bindFull(FileInfo file) {
        assert constraint != null;
        cardView.setCardBackgroundColor(WHITE);
        bindTitle(file);
        bindSummary(file);
        bindLink(file);
        bindImage(file, retrieveThumbnail());
    }

    private void bindPartial(List<Object> payloads) {
        for (Object payload : payloads) {
            if (payload instanceof Bitmap) {
                bindImage(item(), createdRoundedThumbnail((Bitmap) payload));
                imageView.setAlpha(0f);
                imageView.animate().alpha(1f);
            }
        }
    }

    private void bindTitle(FileInfo file) {
        titleView.setText(file.name());
        titleView.setEnabled(file.isReadable());
    }

    private void bindSummary(FileInfo file) {
        String summary = FileTextLayouts.getSummary(context(), file);
        if (summary != null) {
            summaryView.setText(summary);
            summaryView.setVisibility(VISIBLE);
        } else {
            summaryView.setText("");
            summaryView.setVisibility(GONE);
        }
        summaryView.setEnabled(file.isReadable());
    }

    private void bindLink(FileInfo file) {
        Path target = file.linkTargetPath();
        if (target != null) {
            linkView.setText(context().getString(R.string.link_x, target.toString()));
            linkView.setVisibility(VISIBLE);
        } else {
            linkView.setText("");
            linkView.setVisibility(GONE);
        }
        linkView.setEnabled(file.isReadable());
    }

    private Rect calculateThumbnailConstraint(CardView card) {
        Resources res = card.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        int columns = res.getInteger(R.integer.files_grid_columns);
        int maxThumbnailWidth = calculateCardContentWidthPixels(card, columns);
        int maxThumbnailHeight = (int) (metrics.heightPixels * 1.5);
        return Rect.of(maxThumbnailWidth, maxThumbnailHeight);
    }

    private void bindImage(FileInfo file, Drawable preview) {
        assert constraint != null;
        setMarginTop(imageView, (constraint.width() - preview.getIntrinsicWidth()) / 2);
        imageView.setImageDrawable(preview);
        imageView.setEnabled(file.isReadable());
    }

    private Drawable retrieveThumbnail() {

        if (task != null) {
            task.cancel(true);
            task = null;
        }

        Path file = previewPath();
        Stat stat = previewStat();
        assert constraint != null;
        if (stat == null || !decorator.isPreviewable(file, stat, constraint)) {
            backgroundBlurClear();
            return newIcon();
        }

        Bitmap blurred = decorator.getBlurredThumbnail(file, stat, constraint, false);
        if (blurred != null) {
            backgroundBlurSet(blurred);
        } else {
            backgroundBlurClear();
        }

        Bitmap thumbnail = getCachedThumbnail(file, stat);
        if (thumbnail != null) {
            if (blurred == null) {
                // TODO
            }
            return createdRoundedThumbnail(thumbnail);
        }

        runWhenUiIsIdle(file, canInterruptScrollState, () ->
                task = decorator.get(file, stat, constraint, FileViewHolder.this, context()));

        Rect size = decorator.getSize(file, stat, constraint, false);
        if (size != null) {
            Rect scaledSize = scaleSize(size);
            return new SizedColorDrawable(
                    TRANSPARENT,
                    scaledSize.width(),
                    scaledSize.height());
        }

        return newIcon();
    }

    private Drawable newIcon() {
        int resId = item().iconDrawableResourceId();
        Drawable drawable = DrawableCompat.wrap(getDrawable(context(), resId)).mutate();
        setTintList(drawable, titleView.getTextColors());
        setIconAlpha(drawable);
        return drawable;
    }

    private void setIconAlpha(Drawable drawable) {
        drawable.setAlpha(125);
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
        blurView.setBackground(null);
    }

    private void backgroundBlurSet(Bitmap bitmap) {
        RoundedBitmapDrawable drawable = createdRoundedThumbnail(bitmap);
        drawable.setAlpha((int) (0.5f * 255));
        blurView.setBackground(drawable);
    }

    private RoundedBitmapDrawable createdRoundedThumbnail(Bitmap bitmap) {
        Resources res = resources();
        RoundedBitmapDrawable drawable =
                RoundedBitmapDrawableFactory.create(res, bitmap);
        drawable.setCornerRadius(res.getDimension(
                R.dimen.files_item_card_inner_radius));
        return drawable;
    }

    private void backgroundBlurFadeIn(Bitmap thumbnail) {
        backgroundBlurSet(thumbnail);
        blurView.setAlpha(0f);
        blurView.animate().alpha(1);
    }

    @Override
    public void onPreviewAvailable(Path path, Stat stat, Bitmap bm) {
        runWhenUiIsIdle(path, canUpdate, () -> {
            int position = getAdapterPosition();
            if (position != NO_POSITION) {
                recyclerView.getAdapter().notifyItemChanged(position, bm);
            }
        });
    }

    @Override
    public void onBlurredThumbnailAvailable(
            Path path,
            Stat stat,
            Bitmap thumbnail
    ) {
        runWhenUiIsIdle(path, canUpdate, () -> backgroundBlurFadeIn(thumbnail));
    }

    private void runWhenUiIsIdle(
            Path previewPath,
            Provider<Boolean> canUpdate,
            Runnable update
    ) {
        new Runnable() {
            @Override
            public void run() {
                if (!previewPath.equals(previewPath())) {
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
    public void onPreviewFailed(Path path, Stat stat, Object cause) {

        if (isDebugBuild(context())) {
            if (cause instanceof Throwable) {
                Log.d(getClass().getSimpleName(),
                        "No preview " + path, (Throwable) cause);
            } else {
                Log.d(getClass().getSimpleName(),
                        "No preview " + path + " (" + cause + ")");
            }
        }
    }

    @Override
    public void onDestroy() {
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onActivated(boolean activated) {
        if (activated) {
            int color = getColor(context(), R.color.activated_highlight);
            imageView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        } else {
            imageView.setColorFilter(null);
            Drawable drawable = imageView.getDrawable();
            if (!(drawable instanceof RoundedBitmapDrawable)) {
                setIconAlpha(drawable);
            }
        }
    }

    private static void setMarginTop(View view, int pixels) {
        DisplayMetrics metrics = view.getResources().getDisplayMetrics();
        int max = (int) applyDimension(COMPLEX_UNIT_DIP, 18, metrics);
        if (pixels > max) {
            pixels = max;
        }
        MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
        params.topMargin = pixels;
        view.setLayoutParams(params);
    }
}
