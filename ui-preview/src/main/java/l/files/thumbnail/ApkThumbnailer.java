package l.files.thumbnail;

import android.content.pm.PackageManager;

import java.io.IOException;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.base.content.pm.Packages.getApkIconBitmap;

public final class ApkThumbnailer implements Thumbnailer<Path> {

    private final PackageManager packageManager;

    public ApkThumbnailer(PackageManager packageManager) {
        this.packageManager = requireNonNull(packageManager, "packageManager");
    }

    @Override
    public ScaledBitmap create(Path path, Rect max) throws IOException {
        return getApkIconBitmap(path.toString(), max, packageManager);
    }
}
