package l.files.features.object;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Instrumentation;
import android.widget.Button;
import android.widget.EditText;

import l.files.ui.FilesActivity;
import l.files.ui.menu.NewDirFragment;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static junit.framework.Assert.assertEquals;
import static l.files.features.object.Instrumentations.awaitOnMainThread;

public class UiNewFolder {

  private final Instrumentation instrument;
  private final FilesActivity activity;

  public UiNewFolder(Instrumentation instrument, FilesActivity activity) {
    this.instrument = instrument;
    this.activity = activity;
  }

  public UiNewFolder setFilename(final String name) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        filename().setText(name);
      }
    });
    return this;
  }

  public EditText filename() {
    return (EditText) dialog().findViewById(android.R.id.text1);
  }

  private AlertDialog dialog() {
    return (AlertDialog) fragment().getDialog();
  }

  private DialogFragment fragment() {
    return (DialogFragment) activity.getFragmentManager().findFragmentByTag(NewDirFragment.TAG);
  }

  public UiFileActivity ok() {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        okButton().performClick();
      }
    });
    return new UiFileActivity(instrument, activity);
  }

  private Button okButton() {
    return dialog().getButton(BUTTON_POSITIVE);
  }

  public UiNewFolder assertFilename(final String name) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertEquals(name, filename().getText().toString());
      }
    });
    return this;
  }

  public UiNewFolder assertError(final CharSequence error) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertEquals(error, filename().getError());
      }
    });
    return this;
  }

  public UiNewFolder assertOkButtonEnabled(final boolean enabled) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertEquals(enabled, okButton().isEnabled());
      }
    });
    return this;
  }
}
