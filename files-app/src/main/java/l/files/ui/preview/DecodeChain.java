package l.files.ui.preview;

import android.graphics.Bitmap;

import com.google.common.base.Stopwatch;
import com.google.common.net.MediaType;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import l.files.common.graphics.Rect;
import l.files.fs.MagicDetector;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static l.files.common.base.Stopwatches.startWatchIfDebug;
import static l.files.ui.preview.DecodeAudio.isAudio;
import static l.files.ui.preview.DecodeImage.isImage;
import static l.files.ui.preview.DecodePdf.isPdf;
import static l.files.ui.preview.DecodeVideo.isVideo;

final class DecodeChain extends Decode {

  // No need to set UncaughtExceptionHandler to terminate
  // on exception already set by Android
  private static final Executor executor =
      newFixedThreadPool(5, new ThreadFactoryBuilder()
          .setNameFormat("decode-chain-pool-thread-%d")
          .build());

  DecodeChain(
      Resource res,
      Stat stat,
      Rect constraint,
      PreviewCallback callback,
      Preview context) {
    super(res, stat, constraint, callback, context);
  }

  @Override DecodeChain executeOnPreferredExecutor() {
    return (DecodeChain) executeOnExecutor(executor);
  }

  @Nullable static Decode run(
      Resource res,
      Stat stat,
      Rect constraint,
      PreviewCallback callback,
      Preview context) {

    if (!context.isPreviewable(res, stat, constraint)) {
      return null;
    }

    Bitmap cached = context.getBitmap(res, stat, constraint);
    if (cached != null) {
      callback.onPreviewAvailable(res, cached);
      return null;
    }

    Rect size = context.getSize(res, stat, constraint);
    if (size != null) {
      callback.onSizeAvailable(res, size);
    }

    return new DecodeChain(res, stat, constraint, callback, context)
        .executeOnPreferredExecutor();
  }

  @Override protected Object doInBackground(Object... params) {
    if (isCancelled()) {
      return null;
    }

    if (checkNotPreviewable()) {
      return null;
    }

    if (checkIsCache()) {
      return null;
    }

    if (checkBitmapMemCache()) {
      return null;
    }

    Rect size = context.getSize(res, stat, constraint);
    if (size == null) {
      /*
       * Currently decoding the size is much quicker
       * than decoding anything else.
       */
      size = context.decodeSize(res);
      if (size != null) {
        publishProgress(size);
      }
    }

    if (checkBitmapDiskCache()) {
      return null;
    }

    if (isCancelled()) {
      return null;
    }

    MediaType media = checkMediaType();
    if (media == null) {
      return null;
    }

    if (isCancelled()) {
      return null;
    }

    if (isImage(media)) {
      publishProgress(new DecodeImage(
          res, stat, constraint, callback, context));

    } else if (isPdf(media, res)) {
      publishProgress(new DecodePdf(
          res, stat, constraint, callback, context));

    } else if (isAudio(media, res)) {
      publishProgress(new DecodeAudio(
          res, stat, constraint, callback, context, media));

    } else if (isVideo(media, res)) {
      publishProgress(new DecodeVideo(
          res, stat, constraint, callback, context));

    } else {
      publishProgress(NoPreview.INSTANCE);
    }

    return null;
  }

  private boolean checkIsCache() {
    if (res.hierarchy().contains(context.cacheDir)) {
      publishProgress(NoPreview.INSTANCE);
      return true;
    }
    return false;
  }

  private boolean checkNotPreviewable() {
    if (!context.isPreviewable(res, stat, constraint)) {
      publishProgress(NoPreview.INSTANCE);
      return true;
    }
    return false;
  }

  private boolean checkBitmapMemCache() {
    Bitmap bitmap = context.getBitmap(res, stat, constraint);
    if (bitmap != null) {
      publishProgress(bitmap);
      return true;
    }
    return false;
  }

  private boolean checkBitmapDiskCache() {
    Bitmap bitmap = null;
    try {
      bitmap = context.getBitmapFromDisk(res, stat, constraint);
    } catch (Exception e) {
      log.error(e);
    }
    if (bitmap != null) {
      publishProgress(bitmap);
      return true;
    }
    return false;
  }

  private MediaType checkMediaType() {
    MediaType media = context.getMediaType(res, stat, constraint);
    if (media == null) {
      media = decodeMedia();
      if (media != null) {
        publishProgress(media);
      }
    }
    if (media == null) {
      publishProgress(NoPreview.INSTANCE);
    }
    return media;
  }

  private MediaType decodeMedia() {
    try {

      Stopwatch watch = startWatchIfDebug();
      MediaType media = MagicDetector.INSTANCE.detect(res);
      log.debug("media %s %s %s", media, watch, res);
      return media;

    } catch (Exception e) {
      log.error(e);
      return null;
    }
  }
}
