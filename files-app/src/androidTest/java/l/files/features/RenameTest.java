package l.files.features;

import java.io.File;

import l.files.R;
import l.files.features.objects.UiRename;
import l.files.test.BaseFilesActivityTest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.common.testing.Tests.assertExists;
import static l.files.common.testing.Tests.assertNotExists;
import static l.files.common.testing.Tests.timeout;

public final class RenameTest extends BaseFilesActivityTest {

  public void testRenamesFile() throws Throwable {
    final File from = dir().createFile("a");
    final File to = new File(dir().get(), "abc");
    assertFalse(to.exists());

    rename(from).setFilename(to.getName()).ok();

    timeout(1, SECONDS, new Runnable() {
      @Override
      public void run() {
        assertNotExists(from);
        assertExists(to);
      }
    });
  }

  public void testHighlightsFileBaseNameInDialog() {
    File file = dir().createFile("abc.txt");
    rename(file).assertFilenameSelection("abc");
  }

  public void testUsesFilenameAsDefaultText() {
    File file = dir().createFile("a");
    rename(file).assertFilename(file.getName());
  }

  public void testDisablesOkButtonWithNoErrorInitiallyBecauseWeUseSourceFilenameAsSuggestion() {
    rename(dir().createDir("a"))
        .assertOkButtonEnabled(false)
        .assertHasNoError();
  }

  public void testCanNotRenameIfNewNameExists() {
    dir().createFile("abc");
    rename(dir().createFile("a"))

        .setFilename("abc")
        .assertOkButtonEnabled(false)
        .assertHasError(R.string.name_exists)

        .setFilename("ab")
        .assertOkButtonEnabled(true)
        .assertHasNoError();
  }

  public void testRenameButtonIsDisableIfThereAreMoreThanOneFileChecked() {
    File f1 = dir().createDir("dir");
    File f2 = dir().createFile("a");

    screen()
        .check(f1, true)
        .check(f2, true)
        .assertCanRename(false)

        .check(f1, false)
        .assertCanRename(true);
  }

  private UiRename rename(File file) {
    return screen().check(file, true).rename();
  }
}
