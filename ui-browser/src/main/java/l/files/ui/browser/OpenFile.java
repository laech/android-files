package l.files.ui.browser;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;

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

    private final WeakReference<Context> contextRef;
    private final Path file;
    private final Stat stat;

    OpenFile(Context context, Path file, Stat stat) {
        this.contextRef = new WeakReference<>(context);
        this.file = requireNonNull(file);
        this.stat = requireNonNull(stat);
    }

    @Override
    protected Object doInBackground(Void... params) {
        Context context = contextRef.get();
        if (context == null) return null;
        try {
            return MediaTypes.detect(context, file.toJavaPath(), stat);
        } catch (IOException e) {
            return e;
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        Context context = contextRef.get();
        if (context == null) return;
        if (result == null) return;
        if (result instanceof IOException) {
            showException((IOException) result, context);
            return;
        }
        if (!showFile((String) result, context) &&
                !showFile(MediaTypes.generalize((String) result), context)) {
            showFile(MEDIA_TYPE_ANY, context);
        }
    }

    private void showException(IOException exception, Context context) {
        String msg = message(exception);
        makeText(context, msg, LENGTH_SHORT).show();
    }

    private boolean showFile(String media, Context context) {
        debug(media, context);

        try {
            Intent intent = new Intent(ACTION_VIEW);
            intent.setDataAndType(file.toUri(), media);
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    private void debug(String media, Context context) {
        if (isDebugBuild(context)) {
            makeText(context, "[DEBUG] " + media, LENGTH_SHORT).show();
        }
    }
}
