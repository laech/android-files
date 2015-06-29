package l.files.common.graphics;

import auto.parcel.AutoParcel;

import static com.google.common.base.Preconditions.checkArgument;

@AutoParcel
public abstract class ScaledSize
{
    ScaledSize()
    {
    }

    public abstract int originalWidth();

    public abstract int originalHeight();

    public abstract int scaledWidth();

    public abstract int scaledHeight();

    public abstract float scale();

    public static ScaledSize of(
            final int originalWidth,
            final int originalHeight,
            final int scaledWidth,
            final int scaledHeight,
            final float scale)
    {
        checkArgument(originalWidth > 0);
        checkArgument(originalHeight > 0);
        checkArgument(scaledWidth > 0);
        checkArgument(scaledHeight > 0);
        checkArgument(scale > 0);

        return new AutoParcel_ScaledSize(
                originalWidth,
                originalHeight,
                scaledWidth,
                scaledHeight,
                scale);
    }
}
