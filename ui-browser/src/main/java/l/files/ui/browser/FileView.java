package l.files.ui.browser;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import l.files.fs.Stat;
import l.files.ui.base.fs.FileIcons;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.preview.Rect;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.support.v4.content.ContextCompat.getColorStateList;
import static l.files.ui.base.fs.FileIcons.defaultDirectoryIconStringId;
import static l.files.ui.base.fs.FileIcons.fileIconStringId;
import static l.files.ui.base.fs.FileIcons.unknownIconStringId;

public final class FileView extends View implements Drawable.Callback {

    private static TextPaint fileTypeIconPaint;
    private static float fileTypeIconSize;

    private static CharSequence linkArrow;
    private static TextPaint linkArrowPaint;
    private static float linkArrowSize;

    private static ColorStateList primaryColor;
    private static ColorStateList primaryColorInverse;

    private static ColorStateList secondaryColor;
    private static ColorStateList secondaryColorInverse;

    private static float namePaddingTop;
    private static float textPaddingTop;

    private static int transitionDuration;

    private final ThumbnailTransitionDrawable preview;

    private CharSequence fileTypeIcon;
    private Layout name;
    private Layout link;
    private Layout summary;
    private boolean previewNeedsPaddingTop;

    private boolean showLinkIcon;
    private boolean useInverseTextColor;

    {
        Context context = getContext();
        Resources res = getResources();

        if (fileTypeIconPaint == null) {

            fileTypeIconSize = res.getDimension(R.dimen.files_item_icon_text_size);
            fileTypeIconPaint = new TextPaint(ANTI_ALIAS_FLAG);
            fileTypeIconPaint.setTypeface(FileIcons.font(context.getAssets()));
            fileTypeIconPaint.setTextSize(fileTypeIconSize);

            linkArrowSize = res.getDimension(R.dimen.files_item_summary_size);
            linkArrowPaint = new TextPaint(ANTI_ALIAS_FLAG);
            linkArrowPaint.setTextSize(linkArrowSize);
            linkArrow = context.getText(R.string.link_icon);

            primaryColor = getColorStateList(context, R.color.item_text_primary);
            secondaryColor = getColorStateList(context, R.color.item_text_secondary);
            primaryColorInverse = getColorStateList(context, R.color.item_text_primary_inverse);
            secondaryColorInverse = getColorStateList(context, R.color.item_text_secondary_inverse);

            textPaddingTop = res.getDimension(
                    R.dimen.files_item_summary_padding_top);

            namePaddingTop = res.getDimension(
                    R.dimen.files_item_name_padding_top);

            transitionDuration = res.getInteger(
                    android.R.integer.config_shortAnimTime);

        }

        preview = new ThumbnailTransitionDrawable(context, res.getDimension(
                R.dimen.files_item_card_inner_radius));

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

    private CharSequence getIconText(FileInfo item) {
        int id = getIconTextId(item);
        CharSequence text = icons.get(id);
        if (text == null) {
            text = getResources().getText(id);
            icons.put(id, text);
        }
        return text;
    }

    private int getIconTextId(FileInfo item) {
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

        canvas.save();

        int nameColor = getColor(primaryColor, primaryColorInverse);
        int summaryColor = getColor(secondaryColor, secondaryColorInverse);

        float dxPreview = 0;
        float dyPreview = 0;
        if (preview.hasVisibleContent()) {
            dxPreview = (getMeasuredWidth() - preview.getIntrinsicWidth()) / 2F;
            if (previewNeedsPaddingTop) {
                dyPreview = namePaddingTop;
            }
            canvas.translate(dxPreview, dyPreview);
            preview.draw(canvas);

        } else if (fileTypeIcon != null) {
            drawFileTypeIcon(canvas, summaryColor);
        }

        drawName(canvas, nameColor, dxPreview);
        drawLink(canvas, summaryColor);
        drawSummary(canvas, summaryColor);

        canvas.restore();

    }

    private void drawName(Canvas canvas, int color, float dxPreview) {
        canvas.translate(
                getPaddingStart() - dxPreview,
                preview.hasVisibleContent()
                        ? preview.getIntrinsicHeight() + namePaddingTop
                        : getPaddingTop() + fileTypeIconSize + namePaddingTop
        );

        if (name.getPaint().getColor() != color) {
            name.getPaint().setColor(color);
        }

        name.draw(canvas);
        canvas.translate(0, name.getHeight());
    }

    private void drawLink(Canvas canvas, int color) {
        if (link == null) {
            return;
        }
        canvas.translate(0, textPaddingTop);
        if (link.getPaint().getColor() != color) {
            link.getPaint().setColor(color);
        }
        link.draw(canvas);
        canvas.translate(0, link.getHeight());
    }

    private void drawSummary(Canvas canvas, int color) {
        if (summary == null) {
            return;
        }
        canvas.translate(0, textPaddingTop);
        if (summary.getPaint().getColor() != color) {
            summary.getPaint().setColor(color);
        }
        summary.draw(canvas);
    }

    private void drawFileTypeIcon(Canvas canvas, int color) {

        if (fileTypeIconPaint.getColor() != color) {
            fileTypeIconPaint.setColor(color);
        }

        float fileTypeIconX = (getMeasuredWidth() - fileTypeIconSize) / 2;
        float fileTypeIconY = getPaddingTop() + fileTypeIconSize;
        canvas.drawText(
                fileTypeIcon,
                0,
                fileTypeIcon.length(),
                fileTypeIconX,
                fileTypeIconY,
                fileTypeIconPaint);

        if (showLinkIcon) {
            if (linkArrowPaint.getColor() != color) {
                linkArrowPaint.setColor(color);
            }
            float linkArrowX = fileTypeIconX + fileTypeIconSize;
            float linkArrowY = fileTypeIconY - (linkArrowSize / 2);
            canvas.drawText(
                    linkArrow,
                    0,
                    linkArrow.length(),
                    linkArrowX,
                    linkArrowY,
                    linkArrowPaint);
        }
    }

    void setBlurredBackground(@Nullable Bitmap bitmap) {
        RoundedBitmapDrawable drawable =
                RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        drawable.setAlpha((int) (0.3f * 255));
        drawable.setCornerRadius(preview.getCornerRadius());
        setBackground(drawable);
    }

    void set(FileTextLayoutCache layouts, FileInfo item, int textWidth, Rect size) {
        preview.setSize(size.width(), size.height());
        if (preview.isShowingBitmap()) {
            preview.resetTransition();
        }
        set(layouts, item, textWidth);
    }

    void set(FileTextLayoutCache layouts, FileInfo item, int textWidth, Bitmap bitmap) {
        preview.setBitmap(bitmap);
        if (!preview.isShowingBitmap()) {
            preview.startTransition(0);
        }
        set(layouts, item, textWidth);
    }

    void set(FileTextLayoutCache layouts, FileInfo item, int textWidth) {

        this.showLinkIcon = shouldShowLinkIcon(item);

        int width = textWidth + getPaddingStart() + getPaddingEnd();
        this.name = layouts.getName(getContext(), item, textWidth);
        this.link = layouts.getLink(getContext(), item, textWidth);
        this.summary = layouts.getSummary(getContext(), item, textWidth);
        this.fileTypeIcon = getIconText(item);

        int height;

        if (preview.hasVisibleContent()) {

            height = (int) (preview.getIntrinsicHeight()
                    + namePaddingTop
                    + name.getHeight()
                    + (link == null ? 0 : textPaddingTop + link.getHeight())
                    + (summary == null ? 0 : textPaddingTop + summary.getHeight())
                    + getPaddingBottom());

            if ((previewNeedsPaddingTop = preview.getIntrinsicWidth() < textWidth)) {
                height += namePaddingTop;
            }

        } else {
            height = (int) (getPaddingTop()
                    + fileTypeIconSize
                    + namePaddingTop
                    + name.getHeight()
                    + (link == null ? 0 : textPaddingTop + link.getHeight())
                    + (summary == null ? 0 : textPaddingTop + summary.getHeight())
                    + getPaddingBottom());
        }

        setMinimumWidth(width);
        setMinimumHeight(height);
        invalidate();
    }

    private boolean shouldShowLinkIcon(FileInfo item) {
        Stat stat = item.selfStat();
        return stat != null && stat.isSymbolicLink();
    }

    boolean isLinkIconVisible() {
        return showLinkIcon;
    }

    void startPreviewTransition() {
        preview.startTransition(transitionDuration);
    }

    public boolean hasPreviewContent() {
        return preview.hasVisibleContent();
    }

    void setUseInverseTextColor(boolean useInverseTextColor) {
        if (this.useInverseTextColor != useInverseTextColor) {
            this.useInverseTextColor = useInverseTextColor;
            invalidate();
        }
    }

    private int getColor(ColorStateList normal, ColorStateList inverse) {
        ColorStateList color = useInverseTextColor ? inverse : normal;
        return color.getColorForState(getDrawableState(), color.getDefaultColor());
    }

    Layout getSummary() {
        return summary;
    }

    Layout getLink() {
        return link;
    }
}
