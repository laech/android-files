package l.files.features.objects;

import android.app.AlertDialog;
import android.app.Instrumentation;
import android.widget.Button;
import android.widget.EditText;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import l.files.common.base.Consumer;
import l.files.ui.browser.FilesActivity;
import l.files.ui.newdir.NewDirFragment;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static junit.framework.Assert.assertEquals;
import static l.files.features.objects.Instrumentations.awaitOnMainThread;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class UiNewDir
{

    private final Instrumentation instrument;
    private final FilesActivity activity;

    public UiNewDir(
            final Instrumentation instrument,
            final FilesActivity activity)
    {
        this.instrument = instrument;
        this.activity = activity;
    }

    public UiNewDir setFilename(final String name)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                filename().setText(name);
            }
        });
        return this;
    }

    public EditText filename()
    {
        return (EditText) dialog().findViewById(android.R.id.text1);
    }

    private AlertDialog dialog()
    {
        return fragment().getDialog();
    }

    private NewDirFragment fragment()
    {
        return (NewDirFragment) activity.getFragmentManager()
                .findFragmentByTag(NewDirFragment.TAG);
    }

    public UiFileActivity ok()
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                okButton().performClick();
            }
        });
        return new UiFileActivity(instrument, activity);
    }

    public UiFileActivity okExpectingFailure(final String message)
    {
        @SuppressWarnings("unchecked")
        final Consumer<String>[] original = new Consumer[1];

        @SuppressWarnings("unchecked")
        final Consumer<String> consumer = mock(Consumer.class);
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(final InvocationOnMock i) throws Throwable
            {
                original[0].apply((String) i.getArguments()[0]);
                return null;
            }
        }).when(consumer).apply(anyString());

        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                original[0] = fragment().toaster;
                fragment().toaster = consumer;
            }
        });

        ok();

        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                verify(consumer).apply(message);
            }
        });

        return new UiFileActivity(instrument, activity);
    }

    private Button okButton()
    {
        return dialog().getButton(BUTTON_POSITIVE);
    }

    public UiNewDir assertFilename(final String name)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(name, filename().getText().toString());
            }
        });
        return this;
    }

    public UiNewDir assertError(final CharSequence error)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(error, filename().getError());
            }
        });
        return this;
    }

    public UiNewDir assertOkButtonEnabled(final boolean enabled)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(enabled, okButton().isEnabled());
            }
        });
        return this;
    }
}
