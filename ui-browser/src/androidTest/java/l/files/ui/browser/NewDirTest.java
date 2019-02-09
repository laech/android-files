package l.files.ui.browser;

import androidx.test.runner.AndroidJUnit4;
import android.widget.EditText;

import org.junit.Test;
import org.junit.runner.RunWith;

import l.files.base.Consumer;
import l.files.fs.Permission;
import l.files.testing.fs.Paths;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public final class NewDirTest extends BaseFilesActivityTest {

    // TODO test click ok from keyboard

    @Test
    public void shows_error_message_when_failed_to_create()
            throws Exception {
        Paths.removePermissions(dir(), Permission.write());
        screen()
                .newFolder()
                .setFilename("a")
                .okExpectingFailure("Permission denied");
    }

    @Test
    public void creates_folder_with_name_specified() {
        screen()
                .newFolder()
                .setFilename("a")
                .ok()
                .clickInto(dir().concat("a"));
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

        dir().concat(string(R.string.untitled_dir)).createFile();
        dir().concat(string(R.string.untitled_dir) + " " + 2).createFile();

        screen()
                .newFolder()
                .assertFilename(string(R.string.untitled_dir) + " " + 3);
    }

    @Test
    public void can_not_create_if_folder_with_specified_name_already_exists()
            throws Exception {

        dir().concat("a").createFile();
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
