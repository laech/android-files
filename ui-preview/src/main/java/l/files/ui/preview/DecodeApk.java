package l.files.ui.preview;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import l.files.base.graphics.Rect;
import l.files.fs.Path;
import l.files.fs.Stat;

import static l.files.base.content.pm.Packages.getApkIconBitmap;

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
    Result decode() {
        PackageManager manager = context.context.getPackageManager();
        Bitmap bitmap = getApkIconBitmap(path.toString(), manager);
        if (bitmap == null) {
            return null;
        }
        Rect size = Rect.of(bitmap.getWidth(), bitmap.getHeight());
        return new Result(bitmap, size);
    }

}
