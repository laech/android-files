package l.files.ui.bookmarks;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import l.files.ui.base.fs.FileIcons;

import static android.graphics.Color.WHITE;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.text.TextUtils.TruncateAt.END;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;

public final class BookmarkView extends FrameLayout {

    private static TextPaint iconPaint;
    private static TextPaint titlePaint;

    private static float iconSize = -1;
    private static float titleSize = -1;
    private static float titlePaddingStart = -1;
    private static float titlePaddingBottom = -1;

    private String icon;
    private String title;
    private boolean ellipsize;

    {
        if (iconPaint == null) {

            iconSize = applyDimension(COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
            titleSize = applyDimension(COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            titlePaddingStart = applyDimension(COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
            titlePaddingBottom = applyDimension(COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());

            iconPaint = new TextPaint(ANTI_ALIAS_FLAG);
            iconPaint.setTypeface(FileIcons.font(getContext().getAssets()));
            iconPaint.setTextSize(iconSize);
            iconPaint.setColor(WHITE);

            titlePaint = new TextPaint(ANTI_ALIAS_FLAG);
            titlePaint.setTextSize(titleSize);
            titlePaint.setColor(WHITE);

        }
    }

    public BookmarkView(Context context) {
        super(context);
    }

    public BookmarkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BookmarkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getMeasuredHeight();

        float iconX = getPaddingStart();
        float iconY = (height - iconSize) / 2 + iconSize;
        canvas.drawText(icon, iconX, iconY, iconPaint);

        float titleX = iconX + titlePaddingStart;
        float titleY = (height - titleSize) / 2 + titleSize - titlePaddingBottom;
        if (ellipsize) {
            ellipsize = false;
            float width = getMeasuredWidth() - titleX - getPaddingEnd();
            title = TextUtils.ellipsize(title, titlePaint, width, END).toString();
        }
        canvas.drawText(title, titleX, titleY, titlePaint);

    }

    void set(String icon, String title) {
        this.icon = icon;
        this.title = title;
        this.ellipsize = true;
        invalidate();
    }

}
