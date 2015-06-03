package l.files.features.objects;

import android.app.AlertDialog;
import android.app.Instrumentation;
import android.widget.EditText;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import l.files.common.base.Consumer;
import l.files.ui.FileCreationFragment;
import l.files.ui.browser.FilesActivity;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static l.files.features.objects.Instrumentations.awaitOnMainThread;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public abstract class UiFileCreation<T extends UiFileCreation>
{
    private final Instrumentation in;
    private final FilesActivity activity;
    private final String tag;

    public UiFileCreation(
            final Instrumentation in,
            final FilesActivity activity,
            final String tag)
    {
        this.in = in;
        this.activity = activity;
        this.tag = tag;
    }

    @SuppressWarnings("unchecked")
    private T self()
    {
        return (T) this;
    }

    public T setFilename(final CharSequence name)
    {
        awaitOnMainThread(in, new Runnable()
        {
            @Override
            public void run()
            {
                editText().setText(name);
            }
        });
        return self();
    }

    public UiFileActivity ok()
    {
        awaitOnMainThread(in, new Runnable()
        {
            @Override
            public void run()
            {
                assertTrue(dialog().getButton(BUTTON_POSITIVE).performClick());
            }
        });
        return new UiFileActivity(in, activity);
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

        awaitOnMainThread(in, new Runnable()
        {
            @Override
            public void run()
            {
                original[0] = fragment().toaster;
                fragment().toaster = consumer;
            }
        });

        ok();

        awaitOnMainThread(in, new Runnable()
        {
            @Override
            public void run()
            {
                verify(consumer).apply(message);
            }
        });

        return new UiFileActivity(in, activity);
    }

    public T assertOkButtonEnabled(final boolean enabled)
    {
        awaitOnMainThread(in, new Runnable()
        {
            @Override
            public void run()
            {
                final FileCreationFragment fragment = fragment();
                assertNotNull(fragment);
                assertEquals(
                        enabled,
                        dialog().getButton(BUTTON_POSITIVE).isEnabled());
            }
        });
        return self();
    }

    public T assertHasError(final int resId, final Object... args)
    {
        awaitOnMainThread(in, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(
                        activity.getString(resId, args),
                        editText().getError());
            }
        });
        return self();
    }

    public T assertHasNoError()
    {
        awaitOnMainThread(in, new Runnable()
        {
            @Override
            public void run()
            {
                assertNull(editText().getError());
            }
        });
        return self();
    }

    public T assertError(final CharSequence error)
    {
        awaitOnMainThread(in, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(error, editText().getError());
            }
        });
        return self();
    }

    public EditText editText()
    {
        return (EditText) dialog().findViewById(android.R.id.text1);
    }

    private AlertDialog dialog()
    {
        return fragment().getDialog();
    }

    private FileCreationFragment fragment()
    {
        return (FileCreationFragment) activity.getFragmentManager()
                .findFragmentByTag(tag);
    }

    public T assertFilename(final String name)
    {
        awaitOnMainThread(in, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(name, filename());
            }

        });
        return self();
    }

    private String filename()
    {
        return editText().getText().toString();
    }

    public T assertSelection(final String selection)
    {
        awaitOnMainThread(in, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(selection, selection());
            }
        });
        return self();
    }

    private String selection()
    {
        final EditText text = editText();
        return text.getText().toString().substring(
                text.getSelectionStart(),
                text.getSelectionEnd());
    }
}
