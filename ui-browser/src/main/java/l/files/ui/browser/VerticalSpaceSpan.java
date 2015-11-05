package l.files.ui.browser;

import android.graphics.Paint;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.LineHeightSpan;

final class VerticalSpaceSpan implements LineHeightSpan.WithDensity {

    private final int dp;

    VerticalSpaceSpan(int dp) {
        this.dp = dp;
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
            fm.descent += dp * paint.density;
            fm.bottom += dp * paint.density;
        }
    }

}
