package l.files.ui.open;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.IOException;

import l.files.common.base.Consumer;
import l.files.fs.MagicDetector;
import l.files.fs.Resource;
import l.files.logging.Logger;

import static android.content.Intent.ACTION_VIEW;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static java.util.Objects.requireNonNull;
import static l.files.BuildConfig.DEBUG;
import static l.files.fs.Detector.ANY_TYPE;
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

  private class ShowFileTask extends AsyncTask<Void, Void, Object> {
    private Resource resource;

    ShowFileTask(Resource resource) {
      this.resource = resource;
    }

    @Override protected Object doInBackground(Void... params) {
      try {
        log.verbose("detect start");
        String media = MagicDetector.INSTANCE.detect(resource);
        log.verbose("detect end");
        return media;
      } catch (IOException e) {
        return e;
      }
    }

    @Override protected void onPostExecute(Object result) {
      if (result instanceof IOException) {
        showException((IOException) result);
        return;
      }

      if (!showFile((String) result) &&
          !showFile(generalize((String) result))) {
        showFile(ANY_TYPE);
      }
    }

    public void showException(IOException exception) {
      String msg = message(exception);
      makeText(context, msg, LENGTH_SHORT).show();
    }

    private boolean showFile(String media) {
      debug(media);

      Uri uri = Uri.parse(resource.uri().toString());
      try {
        Intent intent = new Intent(ACTION_VIEW);
        intent.setDataAndType(uri, media);
        context.startActivity(intent);
        return true;
      } catch (ActivityNotFoundException e) {
        return false;
      }
    }

    private String generalize(String media) {
      if (media.startsWith("text/")) return "text/*";
      if (media.startsWith("image/")) return "image/*";
      if (media.startsWith("audio/")) return "audio/*";
      if (media.startsWith("video/")) return "video/*";
      if (media.startsWith("application/")) {
        if (media.contains("json") ||
            media.contains("xml") ||
            media.contains("javascript") ||
            media.contains("x-sh")) {
          return "text/*";
        }
      }
      return media;
    }

    private void debug(String media) {
      if (DEBUG) makeText(context, "[DEBUG] " + media, LENGTH_SHORT).show();
    }
  }
}
