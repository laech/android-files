package l.files.ui.base.graphics.drawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;

public final class Drawables {

    private Drawables() {
    }

    public static Bitmap toBitmap(Drawable drawable) {
        Bitmap bitmap = createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
