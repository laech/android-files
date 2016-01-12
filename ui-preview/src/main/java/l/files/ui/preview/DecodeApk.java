package l.files.ui.preview;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.Stat;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;

final class DecodeApk extends DecodeThumbnail {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean acceptsFileExtension(Path path, String extensionInLowercase) {
            return extensionInLowercase.equals("apk");
        }

        @Override
        public boolean acceptsMediaType(Path path, String mediaTypeInLowercase) {
            return mediaTypeInLowercase.equals("application/zip") &&
                    path.name().ext().equalsIgnoreCase("apk");
        }

        @Override
        public Decode create(
                Path path,
                Stat stat,
                Rect constraint,
                Preview.Callback callback,
                Preview.Using using,
                Preview context) {
            return new DecodeApk(path, stat, constraint, callback, using, context);
        }

    };

    DecodeApk(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
            Preview context) {
        super(path, stat, constraint, callback, using, context);
    }

    @Override
    boolean shouldScale() {
        return false;
    }

    @Override
    Result decode() throws IOException {
        Drawable drawable = loadApkIcon();
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = toBitmap(drawable);
        Rect size = Rect.of(bitmap.getWidth(), bitmap.getHeight());
        return new Result(bitmap, size);
    }

    private Drawable loadApkIcon() {
        PackageManager manager = context.context.getPackageManager();
        String path = this.path.toString();
        PackageInfo info = manager.getPackageArchiveInfo(path, 0);
        if (info == null) {
            return null;
        }
        ApplicationInfo app = info.applicationInfo;
        app.sourceDir = app.publicSourceDir = path;
        return app.loadIcon(manager);
    }

    private Bitmap toBitmap(Drawable drawable) {

        Bitmap bitmap = createBitmap(
                context.displayMetrics,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
