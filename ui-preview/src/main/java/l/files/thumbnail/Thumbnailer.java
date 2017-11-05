package l.files.thumbnail;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.List;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public interface Thumbnailer<T> {

    // TODO
    // Need to update NoPreview cache version to invalidate
    // cache when we add a new decoder so existing files
    // marked as not previewable will get re-evaluated.
    // Order matters, from specific to general
    List<Thumbnailer<Path>> all = unmodifiableList(asList(
            new PathStreamThumbnailer(new SvgThumbnailer()),
            new ImageThumbnailer(),
            new MediaThumbnailer(),
            new PdfThumbnailer(),
            new PathStreamThumbnailer(new TextThumbnailer()),
            new ApkThumbnailer()
    ));

    boolean accepts(Path path, String mediaType);

    @Nullable
    ScaledBitmap create(T input, Rect max, Context context) throws Exception;

}
