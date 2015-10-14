package l.files.ui.preview;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Stat;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;

final class DecodeApk extends DecodeThumbnail {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean accept(File file, String mediaType) {
            return mediaType.equals("application/zip") &&
                    file.name().ext().equalsIgnoreCase("apk");
        }

        @Override
        public Decode create(
                File res,
                Stat stat,
                Rect constraint,
                PreviewCallback callback,
                Preview context) {
            return new DecodeApk(res, stat, constraint, callback, context);
        }

    };

    DecodeApk(
            File file,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(file, stat, constraint, callback, context);
    }

    @Override
    boolean shouldScale() {
        return false;
    }

    @Override
    Result decode() throws IOException {
        Drawable drawable = loadApkIcon();
        Bitmap bitmap = toBitmap(drawable);
        Rect size = Rect.of(bitmap.getWidth(), bitmap.getHeight());
        return new Result(bitmap, size);
    }

    private Drawable loadApkIcon() {
        PackageManager manager = context.context.getPackageManager();
        PackageInfo info = manager.getPackageArchiveInfo(file.path(), 0);
        ApplicationInfo app = info.applicationInfo;
        app.sourceDir = app.publicSourceDir = file.path();
        return app.loadIcon(manager);
    }

    @NonNull
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

    @Override
    AsyncTask<Object, Object, Object> executeOnPreferredExecutor() {
        return execute(THREAD_POOL_EXECUTOR);
    }

}
