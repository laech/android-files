package l.files.ui.preview;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;

public final class Rect {

    private final int width;
    private final int height;

    Rect(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean contains(Rect that) {
        return width() >= that.width() && height() >= that.height();
    }

    public static Rect of(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException();
        }
        return new Rect(width, height);
    }

    public Rect scale(Rect constraint) {
        float widthRatio = constraint.width() / (float) width();
        float heightRatio = constraint.height() / (float) height();
        float scale = min(widthRatio, heightRatio);
        int scaledWith = max(round(width() * scale), 1);
        int scaledHeight = max(round(height() * scale), 1);
        return of(scaledWith, scaledHeight);
    }
}
