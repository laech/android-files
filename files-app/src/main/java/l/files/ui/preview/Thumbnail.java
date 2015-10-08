package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Thumbnail {

    public final Bitmap bitmap;
    public final Type type;

    public Thumbnail(Bitmap bitmap, Type type) {
        this.bitmap = requireNonNull(bitmap);
        this.type = requireNonNull(type);
    }

    public enum Type {

        PICTURE(1),
        ICON(2);

        private static final Type[] VALUES = Type.values();

        final int code;

        Type(int code) {
            this.code = code;
        }

        @Nullable
        static Type ofCode(int code) {
            for (Type type : VALUES) {
                if (type.code == code) {
                    return type;
                }
            }
            return null;
        }

    }

}
