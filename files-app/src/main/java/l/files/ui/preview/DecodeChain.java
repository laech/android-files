package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static l.files.ui.preview.DecodeAudio.isAudio;
import static l.files.ui.preview.DecodeImage.isImage;
import static l.files.ui.preview.DecodePdf.isPdf;
import static l.files.ui.preview.DecodeVideo.isVideo;
import static l.files.ui.preview.Preview.decodePalette;

final class DecodeChain extends Decode {

  // No need to set UncaughtExceptionHandler to terminate
  // on exception already set by Android
  private static final Executor executor = newFixedThreadPool(5);

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

    String media = checkMediaType();
    if (media == null) {
      return null;
    }

    if (isCancelled()) {
      return null;
    }

    if (isImage(media)) {
      publishProgress(new DecodeImage(
          res, stat, constraint, callback, context));

    } else if (isPdf(media)) {
      publishProgress(new DecodePdf(
          res, stat, constraint, callback, context));

    } else if (isAudio(media)) {
      publishProgress(new DecodeAudio(
          res, stat, constraint, callback, context));

    } else if (isVideo(media)) {
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
      if (context.getPalette(res, stat, constraint) == null) {
        publishProgress(decodePalette(bitmap));
      }
      return true;
    }
    return false;
  }

  private String checkMediaType() {
    String media = context.getMediaType(res, stat, constraint);
    if (media == null) {
      media = decodeMedia();
      if (media != null) {
        context.putMediaType(res, stat, constraint, media);
      }
    }
    if (media == null) {
      publishProgress(NoPreview.INSTANCE);
    }
    return media;
  }

  private String decodeMedia() {
    try {

      log.debug("decode media start %s", res);
      String media = res.detectContentMediaType();
      log.debug("decode media end %s %s", media, res);
      return media;

    } catch (Exception e) {
      log.error(e);
      return null;
    }
  }
}
