package l.files.ui.browser;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.common.net.MediaType;

import java.io.IOException;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.common.base.Either;
import l.files.fs.MagicDetector;
import l.files.fs.Resource;

import static android.content.Intent.ACTION_VIEW;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static java.util.Objects.requireNonNull;
import static l.files.BuildConfig.DEBUG;
import static l.files.ui.IOExceptions.message;

final class FileOpener implements Consumer<Resource>
{

    public static FileOpener get(final Context context)
    {
        return new FileOpener(context);
    }

    private final Context context;

    FileOpener(final Context context)
    {
        this.context = requireNonNull(context, "context");
    }

    @Override
    public void apply(final Resource resource)
    {
        new ShowFileTask(resource).execute();
    }

    class ShowFileTask extends AsyncTask<Void, Void, Either<MediaType, IOException>>
    {
        private final Resource resource;

        ShowFileTask(final Resource resource)
        {
            this.resource = resource;
        }

        @Override
        protected Either<MediaType, IOException> doInBackground(
                final Void... params)
        {
            try
            {
                final MediaType result = MagicDetector.INSTANCE.detect(resource);
                return Either.left(result);
            }
            catch (final IOException e)
            {
                return Either.right(e);
            }
        }

        @Override
        protected void onPostExecute(final Either<MediaType, IOException> result)
        {
            final MediaType media = result.left();
            final IOException exception = result.right();
            if (exception != null)
            {
                showException(exception);
                return;
            }
            try
            {
                showFile(media);
            }
            catch (final ActivityNotFoundException e)
            {
                showActivityNotFound();
            }
            debug(media);
        }

        public void showException(final IOException exception)
        {
            final String msg = message(exception);
            makeText(context, msg, LENGTH_SHORT).show();
        }

        private void showActivityNotFound()
        {
            final int msg = R.string.no_app_to_open_file;
            makeText(context, msg, LENGTH_SHORT).show();
        }

        private void showFile(final MediaType media)
                throws ActivityNotFoundException
        {
            final Uri uri = Uri.parse(resource.uri().toString());
            context.startActivity(new Intent(ACTION_VIEW)
                    .setDataAndType(uri, media.toString()));
        }

        private void debug(final MediaType media)
        {
            if (DEBUG)
                makeText(context, "[DEBUG] " + media, LENGTH_SHORT).show();
        }
    }
}
