package l.files.features;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.test.Tests.waitUntilSuccessful;

import android.app.AlertDialog;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import java.io.File;
import l.files.R;
import l.files.app.menu.NewDirFragment;
import l.files.test.BaseFilesActivityTest;

public final class NewFolderTest extends BaseFilesActivityTest {

  public void testCreatesFolderWithNameSpecified() throws Throwable {
    final File file = new File(dir().get(), "test-" + currentTimeMillis());
    clickNewFolderMenu();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        title().setText(file.getName());
        clickOk();
      }
    });
    waitUntilExists(file);
  }

  public void testShowsDialogWithInitialNameSuggestion() throws Throwable {
    clickNewFolderMenu();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        assertEquals(string(R.string.untitled_dir), titleText());
      }
    });
  }

  public void testShowsDialogWithNewNameSuggestionIfInitialNamesAreTaken() throws Throwable {
    dir().newFile(string(R.string.untitled_dir));
    dir().newFile(string(R.string.untitled_dir) + " " + 2);
    clickNewFolderMenu();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        assertEquals(string(R.string.untitled_dir) + " " + 3, titleText());
      }

    });
  }

  public void testCanNotCreateIfFolderWithSpecifiedNameAlreadyExists() throws Throwable {
    dir().newFile("a");
    clickNewFolderMenu();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {

        EditText edit = title();
        edit.setText("a");
        assertEquals(string(R.string.name_exists), edit.getError());
        assertFalse(dialog().getButton(BUTTON_POSITIVE).isEnabled());

        edit.setText("b");
        assertNull(edit.getError());
        assertTrue(dialog().getButton(BUTTON_POSITIVE).isEnabled());
      }
    });
  }

  public void testEditTextIsConstructedCorrectly() throws Throwable {
    clickNewFolderMenu();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        EditText edit = title();
        assertEquals(1, edit.getMaxLines());
        assertEquals(0, edit.getSelectionStart());
        assertEquals(edit.getText().length(), edit.getSelectionEnd());
      }
    });
  }

  private String string(int id) {
    return getActivity().getString(id);
  }

  private DialogFragment fragment() {
    return (DialogFragment) getActivity()
        .getSupportFragmentManager().findFragmentByTag(NewDirFragment.TAG);
  }

  private AlertDialog dialog() {
    return (AlertDialog) fragment().getDialog();
  }

  private EditText title() {
    return (EditText) dialog().findViewById(android.R.id.text1);
  }

  private String titleText() {
    return title().getText().toString();
  }

  private void clickOk() {
    dialog().getButton(BUTTON_POSITIVE).performClick();
  }

  private void clickNewFolderMenu() {
    getInstrumentation().invokeMenuActionSync(getActivity(), R.id.new_dir, 0);
  }

  private void waitUntilExists(final File file) {
    waitUntilSuccessful(new Runnable() {
      @Override public void run() {
        assertTrue(file.exists());
      }
    }, 1, SECONDS);
  }
}
