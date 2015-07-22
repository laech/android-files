package l.files.ui.open;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.common.base.Stopwatch;
import com.google.common.net.MediaType;

import java.io.IOException;

import l.files.common.base.Consumer;
import l.files.common.base.Either;
import l.files.fs.MagicDetector;
import l.files.fs.Resource;
import l.files.logging.Logger;

import static android.content.Intent.ACTION_VIEW;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.google.common.net.MediaType.ANY_AUDIO_TYPE;
import static com.google.common.net.MediaType.ANY_IMAGE_TYPE;
import static com.google.common.net.MediaType.ANY_TEXT_TYPE;
import static com.google.common.net.MediaType.ANY_TYPE;
import static com.google.common.net.MediaType.ANY_VIDEO_TYPE;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.requireNonNull;
import static l.files.BuildConfig.DEBUG;
import static l.files.ui.IOExceptions.message;

public final class FileOpener implements Consumer<Resource> {

  private static final Logger log = Logger.get(FileOpener.class);

  public static FileOpener get(Context context) {
    return new FileOpener(context);
  }

  private Context context;

  FileOpener(Context context) {
    this.context = requireNonNull(context, "context");
  }

  @Override public void apply(Resource resource) {
    new ShowFileTask(resource).execute();
  }

  class ShowFileTask
      extends AsyncTask<Void, Void, Either<MediaType, IOException>> {
    private Resource resource;

    ShowFileTask(Resource resource) {
      this.resource = resource;
    }

    @Override
    protected Either<MediaType, IOException> doInBackground(Void... params) {
      try {
        Stopwatch watch = Stopwatch.createStarted();
        MediaType media = MagicDetector.INSTANCE.detect(resource);
        log.debug("detect took %s", watch);
        return Either.left(media);
      } catch (IOException e) {
        return Either.right(e);
      }
    }

    @Override
    protected void onPostExecute(Either<MediaType, IOException> result) {
      MediaType media = result.left();
      IOException exception = result.right();
      if (exception != null) {
        showException(exception);
        return;
      }

      if (!showFile(media) && !showFile(generalize(media))) {
        showFile(ANY_TYPE);
      }
    }

    public void showException(IOException exception) {
      String msg = message(exception);
      makeText(context, msg, LENGTH_SHORT).show();
    }

    private boolean showFile(MediaType media) {
      debug(media);

      Uri uri = Uri.parse(resource.uri().toString());
      try {
        Intent intent = new Intent(ACTION_VIEW);
        intent.setDataAndType(uri, media.toString());
        context.startActivity(intent);
        return true;
      } catch (ActivityNotFoundException e) {
        return false;
      }
    }

    private MediaType generalize(MediaType media) {
      switch (media.type().toLowerCase(ENGLISH)) {
        case "text":
          return ANY_TEXT_TYPE;

        case "image":
          return ANY_IMAGE_TYPE;

        case "audio":
          return ANY_AUDIO_TYPE;

        case "video":
          return ANY_VIDEO_TYPE;

        case "application": {
          String subtype = media.subtype().toLowerCase(ENGLISH);
          if (subtype.contains("json") || subtype.contains("xml")) {
            return ANY_TEXT_TYPE;
          }
          switch (subtype) {
            case "javascript":
            case "x-sh":
              return ANY_TEXT_TYPE;
          }
        }
      }
      return media;
    }

    private void debug(MediaType media) {
      if (DEBUG) makeText(context, "[DEBUG] " + media, LENGTH_SHORT).show();
    }
  }
}
