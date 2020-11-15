package l.files.ui.browser;

import android.widget.EditText;
import androidx.test.runner.AndroidJUnit4;
import l.files.base.Consumer;
import l.files.fs.Path;
import l.files.testing.fs.Paths;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.attribute.PosixFilePermissions;

import static java.nio.file.Files.createFile;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public final class NewDirTest extends BaseFilesActivityTest {

    // TODO test click ok from keyboard

    @Test
    public void shows_error_message_when_failed_to_create()
        throws Exception {
        Paths.removePermissions(
            Path.of(dir()),
            PosixFilePermissions.fromString("-w--w--w-")
        );
        screen()
            .newFolder()
            .setFilename("a")
            .okExpectingFailure(".+AccessDeniedException.+$");
    }

    @Test
    public void creates_folder_with_name_specified() {
        screen()
            .newFolder()
            .setFilename("a")
            .ok()
            .clickInto(dir().resolve("a"));
    }

    @Test
    public void name_field_has_initial_name_suggestion() {
        screen()
            .newFolder()
            .assertFilename(string(R.string.untitled_dir));
    }

    @Test
    public void name_field_has_new_name_suggestion_if_initial_names_are_taken()
        throws Exception {

        createFile(dir().resolve(string(R.string.untitled_dir)));
        createFile(dir().resolve(string(R.string.untitled_dir) + " " + 2));

        screen()
            .newFolder()
            .assertFilename(string(R.string.untitled_dir) + " " + 3);
    }

    @Test
    public void can_not_create_if_folder_with_specified_name_already_exists()
        throws Exception {

        createFile(dir().resolve("a"));
        screen()
            .newFolder()
            .setFilename("a")
            .assertError(string(R.string.name_exists))
            .assertOkButtonEnabled(false)
            .setFilename("b")
            .assertError(null)
            .assertOkButtonEnabled(true);
    }

    @Test
    public void name_field_is_limited_to_one_line() throws Throwable {
        checkNameField(input -> assertEquals(1, input.getMaxLines()));
    }

    @Test
    public void name_field_has_all_text_selected() throws Throwable {
        checkNameField(input -> {
            assertEquals(0, input.getSelectionStart());
            assertEquals(input.getText().length(), input.getSelectionEnd());
        });
    }

    private void checkNameField(Consumer<EditText> assertion)
        throws Throwable {

        UiNewDir dialog = screen().newFolder();
        runTestOnUiThread(() -> assertion.accept(dialog.editText()));
    }

    private String string(int id) {
        return getActivity().getString(id);
    }

}
