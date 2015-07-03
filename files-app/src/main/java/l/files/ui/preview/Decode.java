package l.files.ui.preview;

import android.os.AsyncTask;
import android.view.View;

import l.files.common.graphics.ScaledSize;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.logging.Logger;

import static java.util.Objects.requireNonNull;
import static l.files.R.id.image_decorator_task;
import static l.files.ui.preview.Preview.errors;
import static l.files.ui.preview.Preview.sizes;

abstract class Decode<R> extends AsyncTask<Void, ScaledSize, R>
{
    final Logger log = Logger.get(getClass());
    final Preview context;
    final Resource res;
    final Stat stat;
    final View view;
    final PreviewCallback callback;
    final String key;

    Decode(
            final Preview context,
            final Resource res,
            final Stat stat,
            final View view,
            final PreviewCallback callback,
            final String key)
    {
        this.context = requireNonNull(context, "context");
        this.res = requireNonNull(res, "res");
        this.stat = requireNonNull(stat, "stat");
        this.key = requireNonNull(key, "key");
        this.view = requireNonNull(view, "view");
        this.callback = requireNonNull(callback, "callback");
    }

    @Override
    protected final void onPreExecute()
    {
        super.onPreExecute();
        view.setTag(image_decorator_task, this);
    }

    @Override
    protected void onProgressUpdate(final ScaledSize... values)
    {
        super.onProgressUpdate(values);
        final ScaledSize size = values[0];
        if (sizes.put(key, size) == null
                && view.getTag(image_decorator_task) == this)
        {
            callback.onSizeAvailable(res, size);
        }
    }

    @Override
    protected final void onPostExecute(final R result)
    {
        super.onPostExecute(result);
        if (result == null)
        {
            onFailed();
        }
        else
        {
            onSuccess(result);
        }
    }

    void onFailed()
    {
        errors.put(key, key);
        sizes.remove(key);
        if (view.getTag(image_decorator_task) == this)
        {
            view.setTag(image_decorator_task, null);
            callback.onPreviewFailed(res);
        }
    }

    void onSuccess(final R result)
    {
    }
}
