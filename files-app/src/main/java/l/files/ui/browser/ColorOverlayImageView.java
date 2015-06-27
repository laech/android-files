package l.files.ui.browser;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import static l.files.R.color.activated_highlight;

public final class ColorOverlayImageView extends ImageView
{
    private int overlayColor;

    {
        overlayColor = getContext().getResources().getColor(activated_highlight);
    }

    public ColorOverlayImageView(
            final Context context)
    {
        super(context);
    }

    public ColorOverlayImageView(
            final Context context,
            final AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ColorOverlayImageView(
            final Context context,
            final AttributeSet attrs,
            final int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public ColorOverlayImageView(
            final Context context,
            final AttributeSet attrs,
            final int defStyleAttr,
            final int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(final Canvas canvas)
    {
        super.onDraw(canvas);
        if (isActivated())
        {
            canvas.drawColor(overlayColor);
        }
    }
}
