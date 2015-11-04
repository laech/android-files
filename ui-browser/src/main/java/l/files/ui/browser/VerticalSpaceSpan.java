package l.files.ui.browser;

import android.graphics.Paint;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.LineHeightSpan;

final class VerticalSpaceSpan implements LineHeightSpan.WithDensity {

    private final int top;
    private final int bottom;

    VerticalSpaceSpan(int space) {
        this(space, space);
    }

    VerticalSpaceSpan(int top, int bottom) {
        this.top = top;
        this.bottom = bottom;
    }

    @Override
    public void chooseHeight(
            CharSequence text,
            int start,
            int end,
            int spanstartv,
            int v,
            Paint.FontMetricsInt fm) {
    }

    @Override
    public void chooseHeight(
            CharSequence text,
            int start,
            int end,
            int spanstartv,
            int v,
            Paint.FontMetricsInt fm,
            TextPaint paint) {

        if (end == ((Spanned) text).getSpanEnd(this)) {
            fm.top -= top * paint.density;
            fm.descent += bottom * paint.density;
            fm.bottom += bottom * paint.density;
        }
    }

}
