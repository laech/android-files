package com.example.files.media;

import static com.example.files.util.Files.getFileExtension;

import java.io.File;
import java.io.IOException;

import org.apache.tika.Tika;

import android.os.AsyncTask;
import android.util.Log;

public class MediaDetector {

    public static interface Callback {
        void onResult(File file, String type);
    }

    private static final class Holder {
        // Lazy initialization holder, initialized on first reference to Holder
        static final Tika sTika = new Tika();
    }

    private static final String TAG = MediaDetector.class.getSimpleName();

    public static final MediaDetector INSTANCE = new MediaDetector();

    MediaDetector() {
    }

    public void detect(final File file, final Callback callback) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                try {
                    return Holder.sTika.detect(file);
                } catch (IOException e) {
                    Log.w(TAG, e);
                    return Medias.get(getFileExtension(file));
                } catch (RuntimeException e) {
                    // All other errors, e.g. file no longer exists,
                    // file has been deleted and recreated as a directory etc
                    Log.w(TAG, e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                callback.onResult(file, result);
            }

        }.execute();
    }
}
