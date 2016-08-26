package l.files.ui.base.graphics;

import javax.annotation.Nullable;

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

    public static Rect of(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException();
        }
        return new Rect(width, height);
    }

    public Rect scale(Rect constraint) {
        if (constraint.width() >= width() && constraint.height() >= height()) {
            return this;
        }
        float widthRatio = constraint.width() / (float) width();
        float heightRatio = constraint.height() / (float) height();
        float scale = min(widthRatio, heightRatio);
        int scaledWith = max(round(width() * scale), 1);
        int scaledHeight = max(round(height() * scale), 1);
        return of(scaledWith, scaledHeight);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rect rect = (Rect) o;

        return width == rect.width && height == rect.height;

    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }

    @Override
    public String toString() {
        return "Rect{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}
