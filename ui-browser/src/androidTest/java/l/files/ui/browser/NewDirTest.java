package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import org.junit.Test;
import org.junit.runner.RunWith;

import l.files.base.Consumer;
import l.files.fs.Permission;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public final class NewDirTest extends BaseFilesActivityTest {

    // TODO test click ok from keyboard

    @Test
    public void shows_error_message_when_failed_to_create()
            throws Exception {
        fs.removePermissions(dir(), Permission.write());
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

        fs.createFile(dir().concat(string(R.string.untitled_dir)));
        fs.createFile(dir().concat(string(R.string.untitled_dir) + " " + 2));

        screen()
                .newFolder()
                .assertFilename(string(R.string.untitled_dir) + " " + 3);
    }

    @Test
    public void can_not_create_if_folder_with_specified_name_already_exists()
            throws Exception {

        fs.createFile(dir().concat("a"));
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
        checkNameField(new Consumer<EditText>() {
            @Override
            public void accept(final EditText input) {
                assertEquals(1, input.getMaxLines());
            }
        });
    }

    @Test
    public void name_field_has_all_text_selected() throws Throwable {
        checkNameField(new Consumer<EditText>() {
            @Override
            public void accept(final EditText input) {
                assertEquals(0, input.getSelectionStart());
                assertEquals(input.getText().length(), input.getSelectionEnd());
            }
        });
    }

    private void checkNameField(final Consumer<EditText> assertion)
            throws Throwable {

        final UiNewDir dialog = screen().newFolder();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertion.accept(dialog.editText());
            }
        });
    }

    private String string(final int id) {
        return getActivity().getString(id);
    }

}
