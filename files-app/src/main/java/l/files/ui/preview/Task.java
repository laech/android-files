package l.files.ui.preview;

import android.os.AsyncTask;
import android.widget.ImageView;

import l.files.common.graphics.ScaledSize;
import l.files.fs.Resource;
import l.files.logging.Logger;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Objects.requireNonNull;
import static l.files.R.id.image_decorator_task;
import static l.files.ui.preview.Preview.errors;
import static l.files.ui.preview.Preview.newPlaceholder;
import static l.files.ui.preview.Preview.sizes;

abstract class Task<C> extends AsyncTask<Void, ScaledSize, C>
{
    final Logger log = Logger.get(getClass());

    final Preview context;
    final ImageView view;
    final Resource res;
    final String key;

    Task(
            final Preview context,
            final ImageView view,
            final Resource res,
            final String key)
    {
        this.context = requireNonNull(context, "context");
        this.view = requireNonNull(view, "view");
        this.res = requireNonNull(res, "res");
        this.key = requireNonNull(key, "key");
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
        sizes.put(key, values[0]);
        if (view.getTag(image_decorator_task) == this)
        {
            view.setVisibility(VISIBLE);
            view.setImageDrawable(newPlaceholder(values[0]));
        }
    }

    @Override
    protected final void onPostExecute(final C result)
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
            view.setVisibility(GONE);
            view.setTag(image_decorator_task, null);
        }
    }

    void onSuccess(final C result)
    {
    }
}
