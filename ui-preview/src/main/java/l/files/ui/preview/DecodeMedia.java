package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Path;
import l.files.fs.Stat;

import static java.util.concurrent.Executors.newFixedThreadPool;

abstract class DecodeMedia extends DecodeThumbnail {

    /**
     * Only 2 thread because there is no 'get scaled down image' from MediaMetadataRetriever,
     * so to avoid loading images that are too big.
     */
    private static final Executor executor = newFixedThreadPool(2, new ThreadFactory() {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "preview-decode-media-" + threadNumber.getAndIncrement());
        }

    });

    DecodeMedia(
            Path path,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(path, stat, constraint, callback, context);
    }

    @Override
    DecodeMedia executeOnPreferredExecutor() {
        return (DecodeMedia) executeOnExecutor(executor);
    }

    @Override
    Result decode() throws IOException {
        if (isCancelled()) {
            return null;
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context.context, Uri.parse(file.toUri().toString()));

            if (isCancelled()) {
                return null;
            }

            Bitmap bitmap = decode(retriever);
            if (bitmap != null) {
                Rect size = Rect.of(bitmap.getWidth(), bitmap.getHeight());
                return new Result(bitmap, size);
            }

        } finally {
            retriever.release();
        }
        return null;
    }

    @Nullable
    abstract Bitmap decode(MediaMetadataRetriever retriever);

}
