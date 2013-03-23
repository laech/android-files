package com.example.files.media;

import static com.example.files.util.Files.getFileExtension;

import java.io.File;
import java.io.IOException;

import org.apache.tika.Tika;

import android.os.AsyncTask;

public class MediaDetector {

    public static interface Callback {
        void onResult(File file, String type);
    }

    private static final class Holder {
        // Lazy initialization holder, initialized on first reference to Holder
        static final Tika sTika = new Tika();
    }

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
                    return Medias.get(getFileExtension(file));
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
