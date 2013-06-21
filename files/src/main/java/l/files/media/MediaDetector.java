package l.files.media;

import android.os.AsyncTask;
import android.util.Log;
import com.squareup.otto.Bus;
import l.files.FilesApp;
import l.files.ui.event.MediaDetectedEvent;
import l.files.util.DebugTimer;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;

public class MediaDetector {

  private static final String TAG = MediaDetector.class.getSimpleName();

  public static final MediaDetector INSTANCE = new MediaDetector();

  private final Bus bus;

  MediaDetector() {
    this(FilesApp.BUS);
  }

  MediaDetector(Bus bus) {
    this.bus = bus;
  }

  public void detect(final File file) {
    new AsyncTask<Void, Void, String>() {

      @Override protected String doInBackground(Void... params) {
        try {

          DebugTimer timer = DebugTimer.start(TAG);
          String mediaType = Holder.TIKA.detect(file);
          timer.log("detected", mediaType);
          return mediaType;

        } catch (IOException e) {
          Log.w(TAG, e);
          return null;
        } catch (RuntimeException e) {
          // All other errors, e.g. file no longer exists,
          // file has been deleted and recreated as a directory etc
          Log.w(TAG, e);
          return null;
        }
      }

      @Override protected void onPostExecute(String mediaType) {
        super.onPostExecute(mediaType);
        bus.post(new MediaDetectedEvent(file, mediaType));
      }

    }.execute();
  }

  private static final class Holder {
    // Lazy initialization holder, initialized on first reference to Holder
    static final Tika TIKA;

    static {
      DebugTimer timer = DebugTimer.start(TAG);
      TIKA = new Tika();
      timer.log("Tika initialization");
    }
  }

}
