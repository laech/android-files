package l.files.ui.newdir;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import l.files.R;
import l.files.common.base.Consumer;
import l.files.fs.Resource;
import l.files.operations.Events;
import l.files.ui.FileCreationFragment;
import l.files.ui.Toaster;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.IOExceptions.message;

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

    private EventBus bus;

    @VisibleForTesting
    public Consumer<String> toaster;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        toaster = new Toaster(getActivity());
        bus = Events.get();
        bus.register(this);

        requestNameSuggestion();
    }

    private void requestNameSuggestion()
    {
        final String name = getString(R.string.untitled_dir);
        final Resource base = parent().resolve(name);
        bus.post(Suggestion.Request.basedOn(base));
    }

    public void onEventBackgroundThread(final Suggestion.Request request)
    {
        final String baseName = request.base().name();
        final Resource parent = request.base().parent();
        assert parent != null;

        Resource resource = request.base();
        try
        {
            for (int i = 2; resource.exists(NOFOLLOW); i++)
            {
                resource = parent.resolve(baseName + " " + i);
            }
        }
        catch (final IOException e)
        {
            bus.post(Suggestion.Failure.causedBy(e));
        }

        bus.post(Suggestion.Completion.suggest(resource));
    }

    public void onEventMainThread(final Suggestion.Completion completion)
    {
        setFilename(completion.suggestion().name());
    }

    public void onEventMainThread(final Suggestion.Failure failure)
    {
        setFilename("");
    }

    private void setFilename(final String name)
    {
        final EditText field = getFilenameField();
        final boolean hasNoBeenChangedByUser = field.getText().length() == 0;
        if (hasNoBeenChangedByUser)
        {
            field.setText(name);
            field.selectAll();
        }
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which)
    {
        requestCreationDir();
    }

    private void requestCreationDir()
    {
        final Resource dir = parent().resolve(getFilename());
        bus.post(Creation.Request.target(dir));
    }

    public void onEventBackgroundThread(final Creation.Request request)
    {
        try
        {
            request.dir().createDirectory();
        }
        catch (final IOException e)
        {
            bus.post(Creation.Failure.causedBy(e));
        }
    }

    public void onEventMainThread(final Creation.Failure failure)
    {
        toaster.apply(message(failure.cause()));
    }

    @Override
    protected int getTitleResourceId()
    {
        return R.string.new_dir;
    }
}
