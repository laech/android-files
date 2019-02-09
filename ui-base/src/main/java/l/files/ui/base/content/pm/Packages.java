package l.files.ui.base.content.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.ui.base.graphics.drawable.Drawables.toBitmap;

public final class Packages {

    private Packages() {
    }

    @Nullable
    static Drawable getApkIconDrawable(String path, PackageManager pm) {
        PackageInfo info = pm.getPackageArchiveInfo(path, 0);
        if (info == null) {
            return null;
        }
        ApplicationInfo app = info.applicationInfo;
        app.sourceDir = app.publicSourceDir = path;
        return app.loadIcon(pm);
    }

    @Nullable
    public static ScaledBitmap getApkIconBitmap(String path, Rect max, PackageManager pm) {
        Drawable drawable = getApkIconDrawable(path, pm);
        if (drawable == null) {
            return null;
        }
        return toBitmap(drawable, max);
    }

}
