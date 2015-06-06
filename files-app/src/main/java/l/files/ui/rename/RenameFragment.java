package l.files.ui.rename;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Pair;
import android.widget.EditText;

import java.io.IOException;

import l.files.R;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.operations.Events;
import l.files.ui.CloseActionModeRequest;
import l.files.ui.FileCreationFragment;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.IOExceptions.message;
import static rx.Observable.just;
import static rx.android.app.AppObservable.bindFragment;
import static rx.schedulers.Schedulers.io;

public final class RenameFragment extends FileCreationFragment
{

    public static final String TAG = RenameFragment.class.getSimpleName();

    private static final String ARG_RESOURCE = "resource";

    static RenameFragment create(final Resource resource)
    {
        final Bundle args = new Bundle(2);
        args.putParcelable(ARG_PARENT_RESOURCE, resource.parent());
        args.putParcelable(ARG_RESOURCE, resource);

        final RenameFragment fragment = new RenameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Subscription highlight = Subscriptions.empty();
    private Subscription rename = Subscriptions.empty();

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        highlight.unsubscribe();
        rename.unsubscribe();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        highlight();
    }

    private Resource resource()
    {
        return getArguments().getParcelable(ARG_RESOURCE);
    }

    private void highlight()
    {
        if (getFilename().isEmpty())
        {
            highlight = bindFragment(this, just(resource()))
                    .subscribeOn(io())
                    .map(new Status())
                    .subscribe(new Highlight());
        }
    }

    private static final class Status
            implements Func1<Resource, Pair<Resource, Stat>>
    {
        @Override
        public Pair<Resource, Stat> call(final Resource resource)
        {
            try
            {
                return Pair.create(resource, resource.stat(NOFOLLOW));
            }
            catch (final IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private final class Highlight extends Subscriber<Pair<Resource, Stat>>
    {
        @Override
        public void onCompleted()
        {
        }

        @Override
        public void onError(final Throwable e)
        {
        }

        @Override
        public void onNext(final Pair<Resource, Stat> pair)
        {
            final Resource resource = pair.first;
            final Stat stat = pair.second;
            final EditText field = getFilenameField();
            if (!getFilename().isEmpty())
            {
                return;
            }
            field.setText(resource.name());
            if (stat.isDirectory())
            {
                field.selectAll();
            }
            else
            {
                field.setSelection(0, resource.name().base().length());
            }
        }
    }

    @Override
    protected CharSequence getError(final Resource target)
    {
        if (resource().equals(target))
        {
            return null;
        }
        return super.getError(target);
    }

    @Override
    protected int getTitleResourceId()
    {
        return R.string.rename;
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which)
    {
        rename();
    }

    private void rename()
    {
        final Resource dst = parent().resolve(getFilename());
        rename = bindFragment(this, just(resource()))
                .subscribeOn(io())
                .doOnNext(new MoveTo(dst))
                .subscribe(Actions.empty(), new MoveFailed());
        Events.get().post(CloseActionModeRequest.INSTANCE);
    }

    private final static class MoveTo implements Action1<Resource>
    {
        private final Resource dst;

        public MoveTo(final Resource dst)
        {
            this.dst = dst;
        }

        @Override
        public void call(final Resource resource)
        {
            try
            {
                resource.moveTo(dst);
            }
            catch (final IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private final class MoveFailed implements Action1<Throwable>
    {
        @Override
        public void call(final Throwable throwable)
        {
            toaster.apply(message((IOException) throwable.getCause()));
        }
    }
}
