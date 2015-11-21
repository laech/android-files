package l.files.ui.browser;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import l.files.fs.Stat;
import l.files.ui.base.fs.FileIcons;
import l.files.ui.browser.BrowserItem.FileItem;
import l.files.ui.preview.Rect;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static l.files.ui.base.fs.FileIcons.defaultDirectoryIconStringId;
import static l.files.ui.base.fs.FileIcons.fileIconStringId;
import static l.files.ui.base.fs.FileIcons.unknownIconStringId;

public final class FileView extends View implements Drawable.Callback {

    private static TextPaint fileTypeIconPaint;
    private static TextPaint linkIconPaint;

    private static float fileTypeIconSize;
    private static float linkIconSize;

    private static CharSequence linkIcon;

    private static float descriptionPaddingTop;

    private static int transitionDuration;

    private final ThumbnailTransitionDrawable preview;

    private CharSequence fileTypeIcon;
    private Layout description;
    private boolean previewNeedsPaddingTop;

    private ColorStateList textColor;

    private boolean showLinkIcon;

    {
        if (fileTypeIconPaint == null) {

            fileTypeIconSize = getResources().getDimension(R.dimen.files_item_icon_text_size);
            fileTypeIconPaint = new TextPaint(ANTI_ALIAS_FLAG);
            fileTypeIconPaint.setTypeface(FileIcons.font(getContext().getAssets()));
            fileTypeIconPaint.setTextSize(fileTypeIconSize);

            linkIconSize = getResources().getDimension(R.dimen.files_item_text_size);
            linkIconPaint = new TextPaint(ANTI_ALIAS_FLAG);
            linkIconPaint.setTextSize(linkIconSize);
            linkIcon = getContext().getText(R.string.link_icon);

            descriptionPaddingTop =
                    getResources().getDimension(R.dimen.files_item_drawable_padding);

            transitionDuration =
                    getResources().getInteger(android.R.integer.config_shortAnimTime);

        }

        preview = new ThumbnailTransitionDrawable(
                getContext(),
                getResources().getDimension(R.dimen.files_item_card_inner_radius));
        preview.setCallback(this);
    }

    public FileView(Context context) {
        super(context);
    }

    public FileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FileView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private static final SparseArray<CharSequence> icons = new SparseArray<>();

    private CharSequence getIconText(FileItem item) {
        int id = getIconTextId(item);
        CharSequence text = icons.get(id);
        if (text == null) {
            text = getResources().getText(id);
            icons.put(id, text);
        }
        return text;
    }

    private int getIconTextId(FileItem item) {
        Stat stat = item.linkTargetOrSelfStat();
        if (stat == null) {
            return unknownIconStringId();
        }

        if (stat.isDirectory()) {
            return defaultDirectoryIconStringId();
        } else {
            return fileIconStringId(item.basicMediaType());
        }
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();

        if (preview.hasVisibleContent()) {
            preview.jumpToCurrentState();
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        boolean visible = visibility == VISIBLE && getVisibility() == VISIBLE;
        if (preview.hasVisibleContent() && visible != preview.isVisible()) {
            preview.setVisible(visible, false);
        }
    }

    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);

        if (preview.hasVisibleContent() && SDK_INT >= LOLLIPOP) {
            preview.setHotspot(x, y);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        if (preview.hasVisibleContent() && preview.isStateful()) {
            preview.setState(getDrawableState());
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        if (preview.hasVisibleContent()) {
            preview.setVisible(visibility == VISIBLE, false);
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (preview.hasVisibleContent()) {
            preview.setVisible(getVisibility() == VISIBLE, false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (preview.hasVisibleContent()) {
            preview.setVisible(false, false);
        }
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);

        if (preview.hasVisibleContent() && SDK_INT >= M) {
            preview.setLayoutDirection(layoutDirection);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == preview || super.verifyDrawable(who);
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        if (this.preview.hasVisibleContent() && this.preview == drawable) {
            invalidate();
        } else {
            super.invalidateDrawable(drawable);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int color = textColor.getColorForState(getDrawableState(), textColor.getDefaultColor());

        canvas.save();

        float dxPreview = 0;
        float dyPreview = 0;
        if (preview.hasVisibleContent()) {
            dxPreview = (getMeasuredWidth() - preview.getIntrinsicWidth()) / 2F;
            if (previewNeedsPaddingTop) {
                dyPreview = descriptionPaddingTop;
            }
            canvas.translate(dxPreview, dyPreview);
            preview.draw(canvas);

        } else if (fileTypeIcon != null) {
            drawFileTypeIcon(canvas, color);
        }

        if (description != null) {

            canvas.translate(
                    getPaddingStart() - dxPreview,
                    preview.hasVisibleContent()
                            ? preview.getIntrinsicHeight() + descriptionPaddingTop
                            : getPaddingTop() + fileTypeIconSize + descriptionPaddingTop
            );

            if (textColor != null) {
                description.getPaint().setColor(color);
            }

            description.draw(canvas);
        }

        canvas.restore();

    }

    private void drawFileTypeIcon(Canvas canvas, int color) {

        fileTypeIconPaint.setColor(color);
        if (fileTypeIconPaint.getAlpha() > 150) {
            fileTypeIconPaint.setAlpha(150);
        }
        float fileTypeIconX = (getMeasuredWidth() - fileTypeIconSize) / 2;
        float fileTypeIconY = getPaddingTop() + fileTypeIconSize;
        canvas.drawText(fileTypeIcon, 0, fileTypeIcon.length(), fileTypeIconX, fileTypeIconY, fileTypeIconPaint);

        if (showLinkIcon) {
            linkIconPaint.setColor(color);
            if (linkIconPaint.getAlpha() > 150) {
                linkIconPaint.setAlpha(150);
            }
            float linkIconX = fileTypeIconX + fileTypeIconSize;
            float linkIconY = fileTypeIconY - (linkIconSize / 2);
            canvas.drawText(linkIcon, 0, linkIcon.length(), linkIconX, linkIconY, linkIconPaint);
        }
    }

    void set(FileTextLayoutCache layouts, FileItem item, int textWidth, Rect size) {
        preview.setSize(size.width(), size.height());
        if (preview.isShowingBitmap()) {
            preview.resetTransition();
        }
        set(layouts, item, textWidth);
    }

    void set(FileTextLayoutCache layouts, FileItem item, int textWidth, Bitmap bitmap) {
        preview.setBitmap(bitmap);
        if (!preview.isShowingBitmap()) {
            preview.startTransition(0);
        }
        set(layouts, item, textWidth);
    }

    void set(FileTextLayoutCache layouts, FileItem item, int textWidth) {

        this.showLinkIcon = shouldShowLinkIcon(item);

        int width = textWidth + getPaddingStart() + getPaddingEnd();
        this.description = layouts.get(getContext(), item, textWidth);
        this.fileTypeIcon = getIconText(item);

        int height;

        if (preview.hasVisibleContent()) {

            height = (int) (preview.getIntrinsicHeight()
                    + descriptionPaddingTop
                    + description.getHeight()
                    + getPaddingBottom());

            if ((previewNeedsPaddingTop = preview.getIntrinsicWidth() < textWidth)) {
                height += descriptionPaddingTop;
            }

        } else {
            height = (int) (getPaddingTop()
                    + fileTypeIconSize
                    + descriptionPaddingTop
                    + description.getHeight()
                    + getPaddingBottom());
        }

        setMinimumWidth(width);
        setMinimumHeight(height);
        invalidate();
    }

    private boolean shouldShowLinkIcon(FileItem item) {
        Stat stat = item.selfStat();
        return stat != null && stat.isSymbolicLink();
    }

    void setTextColor(ColorStateList textColor) {
        this.textColor = textColor;
        invalidate();
    }

    boolean isLinkIconVisible() {
        return showLinkIcon;
    }

    CharSequence getText() {
        return description.getText();
    }

    void startPreviewTransition() {
        preview.startTransition(transitionDuration);
    }

}
