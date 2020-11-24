package l.files.ui.browser;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.textfield.TextInputLayout;
import l.files.base.Consumer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static java.util.Objects.requireNonNull;
import static junit.framework.Assert.*;
import static l.files.ui.browser.Instrumentations.awaitOnMainThread;

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

    T setFilename(CharSequence name) {
        awaitOnMainThread(
            context.getInstrumentation(),
            () -> editText().setText(name)
        );
        return self();
    }

    UiFileActivity ok() {
        awaitOnMainThread(context.getInstrumentation(), () -> {
            AlertDialog dialog = dialog();
            Button button = dialog.getButton(BUTTON_POSITIVE);
            assertTrue(dialog.isShowing());
            assertTrue(button.isEnabled());
            assertTrue(button.isClickable());
            assertTrue(button.performClick());
        });
        return context;
    }

    UiFileActivity okExpectingFailure(String regex) {
        @SuppressWarnings("unchecked")
        Consumer<String>[] original = new Consumer[1];

        List<String> messages = new CopyOnWriteArrayList<>();
        Consumer<String> consumer = input -> {
            original[0].accept(input);
            messages.add(input);
        };

        awaitOnMainThread(context.getInstrumentation(), () -> {
            original[0] = fragment().toaster;
            fragment().toaster = consumer;
        });

        ok();

        awaitOnMainThread(context.getInstrumentation(), () -> {
            assertFalse(messages.isEmpty());
            assertTrue(
                "no match: '" + messages + "'",
                messages.get(0).matches(regex)
            );
        });

        return context;
    }

    T assertOkButtonEnabled(boolean enabled) {
        awaitOnMainThread(context.getInstrumentation(), () -> {
            FileCreationFragment fragment = fragment();
            assertNotNull(fragment);
            assertEquals(
                enabled,
                dialog().getButton(BUTTON_POSITIVE).isEnabled()
            );
        });
        return self();
    }

    T assertHasError(int resId, Object... args) {
        awaitOnMainThread(context.getInstrumentation(), () -> assertEquals(
            context.getActivity().getString(resId, args),
            error()
        ));
        return self();
    }

    T assertHasNoError() {
        awaitOnMainThread(
            context.getInstrumentation(),
            () -> assertNull(error())
        );
        return self();
    }

    T assertError(CharSequence error) {
        awaitOnMainThread(
            context.getInstrumentation(),
            () -> assertEquals(error, error())
        );
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
            .getActivity()
            .getSupportFragmentManager()
            .findFragmentByTag(tag);
        assertNotNull(fragment);
        return fragment;
    }

    T assertFilename(CharSequence name) {
        awaitOnMainThread(
            context.getInstrumentation(),
            () -> assertEquals(name.toString(), filename())
        );
        return self();
    }

    private String filename() {
        return editText().getText().toString();
    }

    T assertSelection(String selection) {
        awaitOnMainThread(
            context.getInstrumentation(),
            () -> assertEquals(selection, selection())
        );
        return self();
    }

    private String selection() {
        EditText text = editText();
        return text.getText().toString().substring(
            text.getSelectionStart(),
            text.getSelectionEnd()
        );
    }
}
