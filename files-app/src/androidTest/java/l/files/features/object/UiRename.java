package l.files.features.object;

import android.app.AlertDialog;
import android.app.Instrumentation;
import android.widget.EditText;

import java.util.concurrent.Callable;

import l.files.ui.FilesActivity;
import l.files.ui.mode.RenameFragment;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static l.files.features.object.Instrumentations.awaitOnMainThread;

public final class UiRename {

  private final Instrumentation in;
  private final FilesActivity activity;

  public UiRename(Instrumentation in, FilesActivity activity) {
    this.in = in;
    this.activity = activity;
  }

  public UiRename setFilename(final CharSequence name) {
    awaitOnMainThread(in, new Runnable() {
      @Override public void run() {
        editText().setText(name);
      }
    });
    return this;
  }

  public UiFileActivity ok() {
    awaitOnMainThread(in, new Runnable() {
      @Override public void run() {
        assertTrue(dialog().getButton(BUTTON_POSITIVE).performClick());
      }
    });
    return new UiFileActivity(in, activity);
  }

  public UiRename assertOkButtonEnabled(final boolean enabled) {
    awaitOnMainThread(in, new Callable<Boolean>() {
      @Override public Boolean call() {
        RenameFragment fragment = fragment();
        if (fragment == null) {
          return null;
        }
        assertEquals(enabled, dialog().getButton(BUTTON_POSITIVE).isEnabled());
        return true;
      }
    });
    return this;
  }

  public UiRename assertHasError(final int resId, final Object... args) {
    awaitOnMainThread(in, new Runnable() {
      @Override public void run() {
        assertEquals(activity.getString(resId, args), editText().getError());
      }
    });
    return this;
  }

  public UiRename assertHasNoError() {
    awaitOnMainThread(in, new Runnable() {
      @Override public void run() {
        assertNull(editText().getError());
      }
    });
    return this;
  }

  private EditText editText() {
    return (EditText) dialog().findViewById(android.R.id.text1);
  }

  private AlertDialog dialog() {
    return fragment().getDialog();
  }

  private RenameFragment fragment() {
    return (RenameFragment) activity.getFragmentManager()
        .findFragmentByTag(RenameFragment.TAG);
  }

  public UiRename assertFilename(final String name) {
    awaitOnMainThread(in, new Runnable() {
      @Override public void run() {
        assertEquals(name, getFilename());
      }

    });
    return this;
  }

  private String getFilename() {
    return editText().getText().toString();
  }

  public UiRename assertFilenameSelection(final String selection) {
    awaitOnMainThread(in, new Runnable() {
      @Override public void run() {
        assertEquals(selection, getFilenameSelection());
      }
    });
    return this;
  }

  private String getFilenameSelection() {
    EditText text = editText();
    return text.getText().toString().substring(
        text.getSelectionStart(),
        text.getSelectionEnd());
  }
}
