package l.files.ui.browser;

import android.text.TextPaint;
import android.text.style.CharacterStyle;

final class MaxAlphaSpan extends CharacterStyle {

    private final int alpha;

    MaxAlphaSpan(int alpha) {
        this.alpha = alpha;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        if (tp.getAlpha() > alpha) {
            tp.setAlpha(alpha);
        }
    }

}
