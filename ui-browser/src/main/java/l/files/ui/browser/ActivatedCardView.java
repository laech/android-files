package l.files.ui.browser;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

import static l.files.ui.browser.R.color.activated_background;

public final class ActivatedCardView extends CardView {

    private final int cardBackgroundColorActivated;
    private int cardBackgroundColorNotActivated;

    {
        cardBackgroundColorActivated = getContext().getResources().getColor(activated_background);
    }

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
        super.setActivated(activated);
        setCardBackgroundColor(activated
                ? cardBackgroundColorActivated
                : cardBackgroundColorNotActivated);
    }

}
