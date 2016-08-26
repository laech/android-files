package l.files.ui.base.content.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import javax.annotation.Nullable;

import l.files.ui.base.graphics.drawable.Drawables;

public final class Packages {

    private Packages() {
    }

    @Nullable
    public static Drawable getApkIconDrawable(String path, PackageManager pm) {
        PackageInfo info = pm.getPackageArchiveInfo(path, 0);
        if (info == null) {
            return null;
        }
        ApplicationInfo app = info.applicationInfo;
        app.sourceDir = app.publicSourceDir = path;
        return app.loadIcon(pm);
    }

    @Nullable
    public static Bitmap getApkIconBitmap(String path, PackageManager pm) {
        Drawable drawable = getApkIconDrawable(path, pm);
        if (drawable == null) {
            return null;
        }
        return Drawables.toBitmap(drawable);
    }

}
