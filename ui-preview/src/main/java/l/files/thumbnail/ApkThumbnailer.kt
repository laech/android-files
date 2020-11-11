package l.files.thumbnail

import android.content.Context
import l.files.ui.base.content.pm.Packages
import l.files.ui.base.graphics.Rect
import java.nio.file.Path

internal object ApkThumbnailer : Thumbnailer<Path> {

  override fun accepts(path: Path, type: String) =
    type == "application/vnd.android.package-archive"

  override fun create(input: Path, max: Rect, context: Context) =
    Packages.getApkIconBitmap(input.toString(), max, context.packageManager)
}
