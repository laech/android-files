package l.files.ui.browser;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import static l.files.ui.R.color.activated_highlight;

public final class ColorOverlayImageView extends ImageView {

    private final int overlayColor;

    {
        overlayColor = getContext().getResources().getColor(activated_highlight);
    }

    public ColorOverlayImageView(Context context) {
        super(context);
    }

    public ColorOverlayImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorOverlayImageView(
            Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ColorOverlayImageView(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isActivated()) {
            canvas.drawColor(overlayColor);
        }
    }

}
