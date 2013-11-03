package l.files.features;

import l.files.R;
import l.files.features.object.UiRename;
import l.files.test.BaseFilesActivityTest;

import java.io.File;

public final class RenameTest extends BaseFilesActivityTest {

    public void testRenamesFile() throws Throwable {
        final File from = dir().newFile();
        final File to = new File(dir().get(), "abc");
        assertFalse(to.exists());

        rename(from).setFilename(to.getName()).ok();

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertFalse(from.exists());
                assertTrue(to.exists());
            }
        });
    }

    public void testHighlightsFileBaseNameInDialog() {
        final File file = dir().newFile("abc.txt");
        final String selection = rename(file).getFilenameSelection();
        assertEquals("abc", selection);
    }

    public void testUsesFilenameAsDefaultText() {
        final File file = dir().newFile();
        final String filename = rename(file).getFilename();
        assertEquals(file.getName(), filename);
    }

    public void testDisablesOkButtonWithNoErrorInitiallyBecauseWeUseSourceFilenameAsSuggestion() {
        rename(dir().newDir())
                .assertOkButtonEnabled(false)
                .assertHasNoError();
    }

    public void testCanNotRenameIfNewNameExists() {
        dir().newFile("abc");
        rename(dir().newFile())

                .setFilename("abc")
                .assertOkButtonEnabled(false)
                .assertHasError(R.string.name_exists)

                .setFilename("ab")
                .assertOkButtonEnabled(true)
                .assertHasNoError();
    }

    public void testRenameButtonIsDisableIfThereAreMoreThanOneFileChecked() {
        final File f1 = dir().newDir();
        final File f2 = dir().newFile();

        screen().check(f1, true)
                .check(f2, true)
                .assertCanRename(false)

                .check(f1, false)
                .assertCanRename(true);
    }

    private UiRename rename(File file) {
        return screen().check(file, true).rename();
    }
}
