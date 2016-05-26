package l.files.ui.browser;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.browser.Instrumentations.awaitOnMainThread;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

abstract class UiFileCreation<T extends UiFileCreation> {

    private final UiFileActivity context;
    private final String tag;

    UiFileCreation(UiFileActivity context, String tag) {
        requireNonNull(context);
        requireNonNull(tag);
        this.context = context;
        this.tag = tag;
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    T setFilename(final CharSequence name) {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                editText().setText(name);
            }
        });
        return self();
    }

    UiFileActivity ok() {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                assertTrue(dialog().getButton(BUTTON_POSITIVE).performClick());
            }
        });
        return context;
    }

    UiFileActivity okExpectingFailure(final String message) {
        @SuppressWarnings("unchecked")
        final Consumer<String>[] original = new Consumer[1];

        @SuppressWarnings("unchecked")
        final Consumer<String> consumer = mock(Consumer.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock i) throws Throwable {
                original[0].apply((String) i.getArguments()[0]);
                return null;
            }
        }).when(consumer).apply(anyString());

        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                original[0] = fragment().toaster;
                fragment().toaster = consumer;
            }
        });

        ok();

        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                verify(consumer).apply(message);
            }
        });

        return context;
    }

    T assertOkButtonEnabled(final boolean enabled) {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                FileCreationFragment fragment = fragment();
                assertNotNull(fragment);
                assertEquals(
                        enabled,
                        dialog().getButton(BUTTON_POSITIVE).isEnabled());
            }
        });
        return self();
    }

    T assertHasError(final int resId, final Object... args) {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                assertEquals(
                        context.activity().getString(resId, args),
                        error());
            }
        });
        return self();
    }

    T assertHasNoError() {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                assertNull(error());
            }
        });
        return self();
    }

    T assertError(final CharSequence error) {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                assertEquals(error, error());
            }
        });
        return self();
    }

    private CharSequence error() {
        View view = dialog().findViewById(R.id.text_layout);
        assertNotNull(view);
        return ((TextInputLayout) view).getError();
    }

    EditText editText() {
        return (EditText) dialog().findViewById(R.id.file_name);
    }

    private AlertDialog dialog() {
        AlertDialog dialog = fragment().getDialog();
        assertNotNull(dialog);
        return dialog;
    }

    private FileCreationFragment fragment() {
        FileCreationFragment fragment = (FileCreationFragment) context
                .activity()
                .getSupportFragmentManager()
                .findFragmentByTag(tag);
        assertNotNull(fragment);
        return fragment;
    }

    T assertFilename(final CharSequence name) {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                assertEquals(name.toString(), filename());
            }

        });
        return self();
    }

    private String filename() {
        return editText().getText().toString();
    }

    T assertSelection(final String selection) {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                assertEquals(selection, selection());
            }
        });
        return self();
    }

    private String selection() {
        EditText text = editText();
        return text.getText().toString().substring(
                text.getSelectionStart(),
                text.getSelectionEnd());
    }
}
