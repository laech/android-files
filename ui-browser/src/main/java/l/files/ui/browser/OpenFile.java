package l.files.ui.browser;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.media.MediaTypes;

import static android.content.Intent.ACTION_VIEW;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.media.MediaTypes.MEDIA_TYPE_ANY;
import static l.files.ui.base.content.Contexts.isDebugBuild;
import static l.files.ui.base.fs.IOExceptions.message;

final class OpenFile extends AsyncTask<Void, Void, Object> {

    private final Context context;
    private final Path file;
    private final Stat stat;

    OpenFile(Context context, Path file, Stat stat) {
        this.context = requireNonNull(context);
        this.file = requireNonNull(file);
        this.stat = requireNonNull(stat);
    }

    @Override
    protected Object doInBackground(Void... params) {
        try {
            return MediaTypes.detect(context, file, stat);
        } catch (IOException e) {
            return e;
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        if (result instanceof IOException) {
            showException((IOException) result);
            return;
        }

        if (!showFile((String) result) &&
                !showFile(MediaTypes.generalize((String) result))) {
            showFile(MEDIA_TYPE_ANY);
        }
    }

    private void showException(IOException exception) {
        String msg = message(exception);
        makeText(context, msg, LENGTH_SHORT).show();
    }

    private boolean showFile(String media) {
        debug(media);

        try {
            Intent intent = new Intent(ACTION_VIEW);
            intent.setDataAndType(file.toUri(), media);
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    private void debug(String media) {
        if (isDebugBuild(context)) {
            makeText(context, "[DEBUG] " + media, LENGTH_SHORT).show();
        }
    }
}
