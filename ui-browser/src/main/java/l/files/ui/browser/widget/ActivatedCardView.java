package l.files.ui.browser.widget;

import android.content.Context;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import l.files.ui.browser.R;

import static androidx.core.content.ContextCompat.getColor;

public final class ActivatedCardView extends CardView {

    private final int cardBackgroundColorActivated;
    private int cardBackgroundColorNotActivated;

    {
        cardBackgroundColorActivated = getColor(getContext(), R.color.activated_background);
    }

    @Nullable
    private ActivatedListener listener;

    public ActivatedCardView(Context context) {
        super(context);
    }

    public ActivatedCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ActivatedCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setCardBackgroundColor(int color) {
        super.setCardBackgroundColor(isActivated() ? cardBackgroundColorActivated : color);
        if (cardBackgroundColorActivated != color) {
            cardBackgroundColorNotActivated = color;
        }
    }

    @Override
    public void setActivated(boolean activated) {
        boolean oldActivated = isActivated();
        super.setActivated(activated);
        setCardBackgroundColor(activated
                ? cardBackgroundColorActivated
                : cardBackgroundColorNotActivated);
        if (oldActivated != activated && listener != null) {
            listener.onActivated(activated);
        }
    }

    @Override
    public void setCardElevation(float radius) {
        if (getCardElevation() != radius) {
            super.setCardElevation(radius);
        }
    }

    public void setActivatedListener(ActivatedListener listener) {
        this.listener = listener;
    }

    public interface ActivatedListener {
        void onActivated(boolean activated);
    }
}
