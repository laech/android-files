package l.files.thumbnail;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.IOException;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.ui.base.content.pm.Packages.getApkIconBitmap;

final class ApkThumbnailer implements Thumbnailer<Path> {

    @Override
    public boolean accepts(Path path, String mediaType) {
        return mediaType.equals("application/vnd.android.package-archive");
    }

    @Override
    public ScaledBitmap create(Path path, Rect max, Context context)
            throws IOException {
        PackageManager manager = context.getPackageManager();
        return getApkIconBitmap(path.toString(), max, manager);
    }
}
