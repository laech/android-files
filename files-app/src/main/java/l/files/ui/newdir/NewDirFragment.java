package l.files.ui.newdir;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import java.io.IOException;

import l.files.R;
import l.files.fs.Resource;
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

public final class NewDirFragment extends FileCreationFragment
{
    public static final String TAG = NewDirFragment.class.getSimpleName();

    static NewDirFragment create(final Resource resource)
    {
        final Bundle bundle = new Bundle(1);
        bundle.putParcelable(ARG_PARENT_RESOURCE, resource);

        final NewDirFragment fragment = new NewDirFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private Subscription suggestion = Subscriptions.empty();
    private Subscription creation = Subscriptions.empty();

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        suggestion.unsubscribe();
        creation.unsubscribe();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        suggestName();
    }

    private void suggestName()
    {
        final String name = getString(R.string.untitled_dir);
        final Resource base = parent().resolve(name);
        suggestion = bindFragment(this, just(base))
                .subscribeOn(io())
                .map(new SuggestName())
                .subscribe(new SetName());
    }

    private static final class SuggestName implements Func1<Resource, Resource>
    {
        @Override
        public Resource call(final Resource base)
        {
            final String baseName = base.name();
            final Resource parent = base.parent();
            assert parent != null;
            Resource resource = base;
            try
            {
                for (int i = 2; resource.exists(NOFOLLOW); i++)
                {
                    resource = parent.resolve(baseName + " " + i);
                }
                return resource;
            }
            catch (final IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private final class SetName extends Subscriber<Resource>
    {
        @Override
        public void onCompleted()
        {
        }

        @Override
        public void onError(final Throwable e)
        {
            set("");
        }

        @Override
        public void onNext(final Resource resource)
        {
            set(resource.name());
        }

        private void set(final String name)
        {
            final EditText field = getFilenameField();
            final boolean notChanged = field.getText().length() == 0;
            if (notChanged)
            {
                field.setText(name);
                field.selectAll();
            }
        }
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which)
    {
        createDir(parent().resolve(getFilename()));
    }

    private void createDir(final Resource dir)
    {
        creation = bindFragment(this, just(dir))
                .subscribeOn(io())
                .doOnNext(new CreateDir())
                .subscribe(Actions.empty(), new CreateFailed());
    }

    private static final class CreateDir implements Action1<Resource>
    {
        @Override
        public void call(final Resource dir)
        {
            try
            {
                dir.createDirectory();
            }
            catch (final IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private final class CreateFailed implements Action1<Throwable>
    {
        @Override
        public void call(final Throwable throwable)
        {
            toaster.apply(message((IOException) throwable.getCause()));
        }
    }

    @Override
    protected int getTitleResourceId()
    {
        return R.string.new_dir;
    }
}
