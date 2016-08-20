package l.files.ui.browser;

import android.widget.EditText;

import l.files.base.Consumer;
import l.files.fs.Permission;

import static l.files.fs.Files.createFile;
import static l.files.fs.Files.removePermissions;

public final class NewDirTest extends BaseFilesActivityTest {

    // TODO test click ok from keyboard

    public void test_shows_error_message_when_failed_to_create()
            throws Exception {
        removePermissions(dir(), Permission.write());
        screen()
                .newFolder()
                .setFilename("a")
                .okExpectingFailure("Permission denied");
    }

    public void test_creates_folder_with_name_specified() {
        screen()
                .newFolder()
                .setFilename("a")
                .ok()
                .clickInto(dir().resolve("a"));
    }

    public void test_name_field_has_initial_name_suggestion() {
        screen()
                .newFolder()
                .assertFilename(string(R.string.untitled_dir));
    }

    public void test_name_field_has_new_name_suggestion_if_initial_names_are_taken()
            throws Exception {

        createFile(dir().resolve(string(R.string.untitled_dir)));
        createFile(dir().resolve(string(R.string.untitled_dir) + " " + 2));

        screen()
                .newFolder()
                .assertFilename(string(R.string.untitled_dir) + " " + 3);
    }

    public void test_can_not_create_if_folder_with_specified_name_already_exists()
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

    public void test_name_field_is_limited_to_one_line() throws Throwable {
        checkNameField(new Consumer<EditText>() {
            @Override
            public void accept(final EditText input) {
                assertEquals(1, input.getMaxLines());
            }
        });
    }

    public void test_name_field_has_all_text_selected() throws Throwable {
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
