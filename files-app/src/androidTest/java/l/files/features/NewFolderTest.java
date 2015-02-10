package l.files.features;

import android.widget.EditText;

import l.files.R;
import l.files.features.objects.UiNewFolder;
import l.files.test.BaseFilesActivityTest;

public final class NewFolderTest extends BaseFilesActivityTest {

  public void testCreatesFolderWithNameSpecified() {
    screen()
        .newFolder()
        .setFilename("a")
        .ok()
        .selectItem(dir().get("a"));
  }

  public void testShowsDialogWithInitialNameSuggestion() {
    screen()
        .newFolder()
        .assertFilename(string(R.string.untitled_dir));
  }

  public void testShowsDialogWithNewNameSuggestionIfInitialNamesAreTaken() {
    dir().createFile(string(R.string.untitled_dir));
    dir().createFile(string(R.string.untitled_dir) + " " + 2);

    screen()
        .newFolder()
        .assertFilename(string(R.string.untitled_dir) + " " + 3);
  }

  public void testCanNotCreateIfFolderWithSpecifiedNameAlreadyExists() {
    dir().createFile("a");
    screen()
        .newFolder()
        .setFilename("a")
        .assertError(string(R.string.name_exists))
        .assertOkButtonEnabled(false)
        .setFilename("b")
        .assertError(null)
        .assertOkButtonEnabled(true);
  }

  public void testEditTextIsConstructedCorrectly() throws Throwable {
    final UiNewFolder dialog = screen().newFolder();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        EditText edit = dialog.filename();
        assertEquals(1, edit.getMaxLines());
        assertEquals(0, edit.getSelectionStart());
        assertEquals(edit.getText().length(), edit.getSelectionEnd());
      }
    });
  }

  private String string(int id) {
    return getActivity().getString(id);
  }
}
