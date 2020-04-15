package l.files.ui.browser;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.appcompat.view.ActionMode;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemAnimator;
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
import l.files.ui.base.fs.OpenFileEvent;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.messaging.MainThreadTopic;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.selection.SelectionModeViewHolder;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.browser.text.FileTextLayouts;
import l.files.ui.browser.widget.ActivatedCardView;
import l.files.ui.browser.widget.ActivatedCardView.ActivatedListener;
import l.files.ui.preview.Preview;
import l.files.ui.preview.PreviewKt;
import l.files.ui.preview.SizedColorDrawable;

import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Color.WHITE;
import static androidx.core.content.ContextCompat.getColor;
import static androidx.core.content.ContextCompat.getDrawable;
import static androidx.core.graphics.drawable.DrawableCompat.setTintList;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.base.content.Contexts.isDebugBuild;
import static l.files.ui.browser.FilesAdapter.calculateCardContentWidthPixels;

public final class FileViewHolder
        extends SelectionModeViewHolder<Path, FileInfo>
        implements Preview.Callback, LifeCycleListener, ActivatedListener {

    static final int LAYOUT_ID = R.layout.files_grid_item;

    private final Preview preview;
    private final MainThreadTopic<OpenFileEvent> topic;

    private final ImageView thumbnailView;
    private final TextView titleView;
    private final TextView linkView;
    private final TextView summaryView;
    private final View backgroundView;
    private final CardView cardView;
    private final RecyclerView recyclerView;

    private Rect constraint;

    @Nullable
    private AsyncTask<?, ?, ?> decodeThumbnailTask;

    FileViewHolder(
            View itemView,
            RecyclerView recyclerView,
            LifeCycleListenable listenable,
            Selection<Path, FileInfo> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback,
            MainThreadTopic<OpenFileEvent> topic
    ) {

        super(itemView, selection, actionModeProvider, actionModeCallback);

        this.recyclerView = requireNonNull(recyclerView, "recyclerView");
        this.preview = PreviewKt.getPreview(itemView.getContext());
        this.topic = requireNonNull(topic, "topic");
        this.backgroundView = itemView.findViewById(R.id.blur);
        this.cardView = itemView.findViewById(R.id.card);
        this.titleView = itemView.findViewById(R.id.title);
        this.linkView = itemView.findViewById(R.id.link);
        this.summaryView = itemView.findViewById(R.id.summary);
        this.thumbnailView = itemView.findViewById(R.id.image);
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
        topic.postOnMainThread(new OpenFileEvent(file.selfPath(), file.linkTargetOrSelfStat()));
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
        cardView.setCardBackgroundColor(WHITE);
        bindTitle(file);
        bindSummary(file);
        bindLink(file);
        bindThumbnail(file, retrieveThumbnail());
    }

    private void bindPartial(List<Object> payloads) {
        for (Object payload : payloads) {
            if (payload instanceof Bitmap) {
                bindThumbnail(item(), createRoundedBitmapDrawable((Bitmap) payload));
                thumbnailView.setAlpha(0f);
                thumbnailView
                        .animate()
                        .setDuration(animateDuration())
                        .alpha(1f);
            }
        }
    }

    private void bindTitle(FileInfo file) {
        titleView.setText(file.name());
        titleView.setEnabled(file.isReadable());
    }

    private void bindSummary(FileInfo file) {
        String summary = FileTextLayouts.getSummary(context(), file);
        summaryView.setText(summary);
        summaryView.setVisibility(summary != null ? VISIBLE : GONE);
        summaryView.setEnabled(file.isReadable());
    }

    private void bindLink(FileInfo file) {
        Path target = file.linkTargetPath();
        linkView.setText(target != null ? getLinkLabel(target) : null);
        linkView.setVisibility(target != null ? VISIBLE : GONE);
        linkView.setEnabled(file.isReadable());
    }

    private String getLinkLabel(Path linkTarget) {
        return context().getString(R.string.link_x, linkTarget.toString());
    }

    private Rect calculateThumbnailConstraint(CardView card) {
        Resources res = card.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        int columns = res.getInteger(R.integer.files_grid_columns);
        int maxThumbnailWidth = calculateCardContentWidthPixels(card, columns);
        int maxThumbnailHeight = (int) (metrics.heightPixels * 1.5);
        return Rect.of(maxThumbnailWidth, maxThumbnailHeight);
    }

    private void bindThumbnail(FileInfo file, Drawable thumbnail) {
        int margin = (constraint.width() - thumbnail.getIntrinsicWidth()) / 2;
        setMarginTop(thumbnailView, margin);
        thumbnailView.setImageDrawable(thumbnail);
        thumbnailView.setEnabled(file.isReadable());
    }

    private Drawable retrieveThumbnail() {

        if (decodeThumbnailTask != null) {
            decodeThumbnailTask.cancel(true);
            decodeThumbnailTask = null;
        }

        Path path = previewPath();
        Stat stat = previewStat();
        if (stat == null || !preview.isPreviewable(path, stat, constraint)) {
            clearBackground();
            return newIcon();
        }

        Bitmap blurred = preview.getBlurredThumbnail(
                path, stat, constraint, false
        );
        if (blurred != null) {
            setBackground(blurred, false);
        } else {
            clearBackground();
        }

        Bitmap thumbnail = getCachedThumbnail(path, stat);
        if (thumbnail != null) {
            // if (blurred == null) {
                // TODO
            //}
            return createRoundedBitmapDrawable(thumbnail);
        }

        runWhenUiIsIdle(path, this::canInterruptScrollState, () ->
                decodeThumbnailTask = preview.get(
                        path, stat, constraint, this, context()));

        return getOrNewIcon(path, stat);
    }

    private Drawable getOrNewIcon(Path path, Stat stat) {
        Rect size = preview.getSize(path, stat, constraint, false);
        if (size != null) {
            Rect scaledSize = scaleSize(size);
            return new SizedColorDrawable(
                    TRANSPARENT,
                    scaledSize.width(),
                    scaledSize.height()
            );
        }
        return newIcon();
    }

    private Drawable newIcon() {
        int resId = item().iconDrawableResourceId();
        Drawable icon = getDrawable(context(), resId);
        assert icon != null;
        Drawable wrapped = DrawableCompat.wrap(icon).mutate();
        setTintList(wrapped, titleView.getTextColors());
        setIconAlpha(wrapped);
        return wrapped;
    }

    private void setIconAlpha(Drawable drawable) {
        drawable.setAlpha(125);
    }

    private boolean canUpdateUi() {
        ItemAnimator animator = recyclerView.getItemAnimator();
        return (animator == null || !animator.isRunning()) &&
                canInterruptScrollState();
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
        int scrollState = recyclerView.getScrollState();
        return scrollState == SCROLL_STATE_IDLE ||
                scrollState == SCROLL_STATE_DRAGGING;
    }

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
        // TODO this will cause thumbnail not to be the latest correct one
        boolean changedMoreThan5SecondsAgo = now - then > 5000;
        if (changedMoreThan5SecondsAgo) {
            return preview.getThumbnail(path, stat, constraint, true);
        } else {
            return preview.getThumbnail(path, stat, constraint, false);
        }
    }

    private Rect scaleSize(Rect size) {
        return size.scaleDown(constraint);
    }

    private void clearBackground() {
        backgroundView.setBackground(null);
    }

    private static final int BG_FILTER = Color.parseColor("#AAFFFFFF");

    private void setBackground(Bitmap bitmap, boolean fade) {
        Drawable drawable = createRoundedBitmapDrawable(bitmap);
        drawable.setColorFilter(BG_FILTER, PorterDuff.Mode.LIGHTEN);
        if (fade) {
            TransitionDrawable background = new TransitionDrawable(
                    new Drawable[]{new ColorDrawable(TRANSPARENT), drawable}
            );
            backgroundView.setBackground(background);
            background.startTransition(animateDuration());
        } else {
            backgroundView.setBackground(drawable);
        }
    }

    private RoundedBitmapDrawable createRoundedBitmapDrawable(Bitmap bitmap) {
        Resources res = resources();
        RoundedBitmapDrawable drawable =
                RoundedBitmapDrawableFactory.create(res, bitmap);
        drawable.setCornerRadius(res.getDimension(
                R.dimen.files_item_card_inner_radius));
        return drawable;
    }

    private int animateDuration() {
        return resources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    @Override
    public void onPreviewAvailable(Path path, Stat stat, Bitmap bm) {
        runWhenUiIsIdle(path, this::canUpdateUi, () -> {
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
        runWhenUiIsIdle(path, this::canUpdateUi,
                () -> setBackground(thumbnail, true));
    }

    private void runWhenUiIsIdle(
            Path previewPath,
            Provider<Boolean> canUpdateUi,
            Runnable update
    ) {
        if (!previewPath.equals(previewPath())) {
            return;
        }
        if (!canUpdateUi.get()) {
            itemView.postDelayed(() ->
                    runWhenUiIsIdle(previewPath, canUpdateUi, update), 50);
            return;
        }
        int position = getAdapterPosition();
        if (position != NO_POSITION) {
            update.run();
        }
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
        if (decodeThumbnailTask != null) {
            decodeThumbnailTask.cancel(true);
            decodeThumbnailTask = null;
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
            thumbnailView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        } else {
            thumbnailView.setColorFilter(null);
            Drawable drawable = thumbnailView.getDrawable();
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
