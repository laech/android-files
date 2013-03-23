package com.example.files.app;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.files.test.Activities.rotate;
import static com.example.files.test.TempDirectory.newTempDirectory;
import static com.example.files.test.TestFileListFragmentActivity.DIRECTORY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.app.FileListFragment.OnFileSelectedListener;
import com.example.files.test.TempDirectory;
import com.example.files.test.TestFileListFragmentActivity;

public final class FileListFragmentTest
        extends ActivityInstrumentationTestCase2<TestFileListFragmentActivity> {

    private TempDirectory mDirectory;

    public FileListFragmentTest() {
        super(TestFileListFragmentActivity.class);
    }

    public void testSortsFilesByName() {
        File z = mDirectory.newFile("z");
        File a = mDirectory.newFile("a");
        File c = mDirectory.newDirectory("C");

        ListView list = getListView();
        assertEquals(a, list.getItemAtPosition(0));
        assertEquals(c, list.getItemAtPosition(1));
        assertEquals(z, list.getItemAtPosition(2));
    }

    public void testShowsCorrectNumSelectedItemsOnRotation() throws Throwable {
        mDirectory.newFile();

        getActivity();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                getListView().setItemChecked(0, true);
                rotate(getActivity());
            }
        });

        assertEquals(
                getString(R.string.n_selected, 1),
                getActivity().getActionMode().getTitle());
    }

    public void testShowsCorrectNumSelectedItemsOnSelection() throws Throwable {
        mDirectory.newFile();
        mDirectory.newFile();

        getActivity();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                getListView().setItemChecked(0, true);
                getListView().setItemChecked(1, true);
            }
        });

        assertEquals(
                getString(R.string.n_selected, 2),
                getActivity().getActionMode().getTitle());
    }

    public void testHidesEmptyViewIfDirectoryHasFile() throws Exception {
        mDirectory.newFile();
        assertEmptyViewIsNotVisible();
    }

    public void testPostsEventOnItemClick() throws Throwable {
        final File file = mDirectory.newFile();
        final OnFileSelectedListener listener = mock(OnFileSelectedListener.class);
        getActivity().getFragment().setListener(listener);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                clickFirstListItem();
            }
        });

        verify(listener).onFileSelected(file);
    }

    public void testShowsEmptyListViewIfDirectoryHasNoFile() {
        assertEquals(0, getListView().getChildCount());
    }

    public void testShowsEmptyMessageIfNoFiles() {
        assertEmptyViewIsVisible(R.string.empty);
    }

    public void testShowsDirectoryNotExistsIfDirectoryDoesNotExist() throws Exception {
        mDirectory.delete();
        assertEmptyViewIsVisible(R.string.directory_doesnt_exist);
    }

    public void testShowsNotDirectoryMessageIfArgIsNotDirectory() throws Exception {
        setTestIntent(mDirectory.newFile());
        assertEmptyViewIsVisible(R.string.not_a_directory);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDirectory = newTempDirectory();
        setTestIntent(mDirectory.get());
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            mDirectory.delete();
        } finally {
            super.tearDown();
        }
    }

    private void assertEmptyViewIsNotVisible() {
        assertEquals(GONE, getEmptyView().getVisibility());
    }

    private void assertEmptyViewIsVisible(int msgId) {
        assertEquals(VISIBLE, getEmptyView().getVisibility());
        assertEquals(getString(msgId), getEmptyView().getText().toString());
    }

    private void clickFirstListItem() {
        assertTrue(getListView().performItemClick(getListView().getChildAt(0), 0, 0));
    }

    private TextView getEmptyView() {
        return (TextView) getActivity().findViewById(android.R.id.empty);
    }

    private FileListFragment getFragment() {
        return getActivity().getFragment();
    }

    private String getString(int resId) {
        return getActivity().getString(resId);
    }

    private String getString(int resId, Object... args) {
        return getActivity().getString(resId, args);
    }

    private ListView getListView() {
        return getFragment().getListView();
    }

    private void setTestIntent(File directory) {
        setActivityIntent(new Intent().putExtra(DIRECTORY, directory.getAbsolutePath()));
    }
}
