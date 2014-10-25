package l.files.app;

import android.content.Intent;
import android.test.UiThreadTest;
import android.view.ActionMode;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.base.Optional;
import l.files.R;
import l.files.test.TempDir;
import l.files.test.TestFilesFragmentActivity;
import l.files.ui.FilesFragment;

import java.io.File;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static l.files.test.TestFilesFragmentActivity.DIRECTORY;

public final class FilesFragmentTest
        extends BaseFileListFragmentTest<TestFilesFragmentActivity> {

    private TempDir mDir;

    public FilesFragmentTest() {
        super(TestFilesFragmentActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDir = TempDir.create();
        setTestIntent(mDir.get());
    }

    @Override
    protected void tearDown() throws Exception {
        mDir.delete();
        super.tearDown();
    }

    @Override
    protected FilesFragment fragment() {
        return getActivity().getFragment();
    }

    @UiThreadTest
    public void testShowsCorrectNumSelectedItemsOnSelection() {
        listView().setItemChecked(0, true);
        listView().setItemChecked(1, true);
        assertEquals(string(R.string.n_selected, 2), actionMode().getTitle());
    }

    @UiThreadTest
    public void testHidesEmptyViewIfHasFile() throws Exception {
        fragment().onRefreshed(Optional.of(asList(new File("/"))));
        assertEmptyViewIsNotVisible();
    }

    @UiThreadTest
    public void testShowsEmptyListViewIfNoFile() {
        fragment().onRefreshed(Optional.of(emptyList()));
        assertEquals(0, listView().getChildCount());
    }

    @UiThreadTest
    public void testShowsEmptyMessageIfNoFiles() {
        fragment().onRefreshed(Optional.of(emptyList()));
        assertEmptyViewIsVisible(R.string.empty);
    }

    @UiThreadTest
    public void testShowsDirNotExistsIfDirectoryDoesNotExist() {
        mDir.delete();
        fragment().onRefreshed(Optional.<List<Object>>absent());
        assertEmptyViewIsVisible(R.string.directory_doesnt_exist);
    }

    public void testShowsNotDirMessageIfArgIsNotDir() throws Throwable {
        setTestIntent(mDir.newFile());
        fragment();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                fragment().onRefreshed(Optional.<List<Object>>absent());
                assertEmptyViewIsVisible(R.string.not_a_directory);
            }
        });
    }

    @UiThreadTest
    public void testShowsEmptyListIfAllFilesAreDeleted() throws Throwable {
        fragment().onRefreshed(Optional.of(emptyList()));
        assertEquals(0, listView().getCount());
    }

    private void assertEmptyViewIsNotVisible() {
        assertEquals(GONE, emptyView().getVisibility());
    }

    private void assertEmptyViewIsVisible(int msgId) {
        assertEquals(VISIBLE, emptyView().getVisibility());
        assertEquals(string(msgId), emptyView().getText());
    }

    private TextView emptyView() {
        return (TextView) getActivity().findViewById(android.R.id.empty);
    }

    private String string(int resId, Object... args) {
        return getActivity().getString(resId, args);
    }

    private ListView listView() {
        return fragment().getListView();
    }

    private void setTestIntent(File dir) {
        setActivityIntent(new Intent().putExtra(DIRECTORY, dir.getAbsolutePath()));
    }

    private ActionMode actionMode() {
        return getActivity().getActionMode();
    }
}
