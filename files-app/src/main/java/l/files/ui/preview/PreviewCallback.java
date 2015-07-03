package l.files.ui.preview;

import android.graphics.Bitmap;

import l.files.common.graphics.ScaledSize;
import l.files.fs.Resource;

public interface PreviewCallback
{

    void onSizeAvailable(final Resource item, ScaledSize size);

    void onPreviewAvailable(final Resource item, Bitmap bitmap);

    void onPreviewFailed(final Resource item);

}
