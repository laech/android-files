package com.example.files.app;

import static android.app.ActionBar.DISPLAY_HOME_AS_UP;
import static android.os.Environment.getExternalStorageDirectory;
import static com.example.files.app.FileListActivity.EXTRA_DIRECTORY;
import static com.example.files.test.Activities.rotate;
import static com.example.files.test.TempDirectory.newTempDirectory;

import java.io.File;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.example.files.R;
import com.example.files.test.TempDirectory;

public final class FileListActivityTest extends ActivityInstrumentationTestCase2<FileListActivity> {

    private TempDirectory mDirectory;

    public FileListActivityTest() {
        super(FileListActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDirectory = newTempDirectory();
    }

    @Override
    protected void tearDown() throws Exception {
        mDirectory.delete();
        super.tearDown();
    }

    public void testHomeButtonIsDisabledWhenNoDirectoryIsSpecified() {
        assertEquals(0, (getActivity().getActionBar().getDisplayOptions() & DISPLAY_HOME_AS_UP));
    }

    public void testHomeButtonIsEnabledWhenDirectoryIsSpecified() {
        setActivityIntent(newIntent(mDirectory.newDirectory()));
        assertTrue(0 < (getActivity().getActionBar().getDisplayOptions() & DISPLAY_HOME_AS_UP));
    }

    public void testShowsTitleCorrectlyOnScreenRotate() throws Throwable {
        setActivityIntent(newIntent(mDirectory.get()));

        getActivity();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                rotate(getActivity());
            }
        });

        assertEquals(mDirectory.get().getName(), getActivity().getActionBar().getTitle());
    }

    public void testShowsTitleUsingNameOfDirectorySpecified() {
        setActivityIntent(newIntent(mDirectory.get()));
        assertEquals(mDirectory.get().getName(), getActivity().getActionBar().getTitle());
    }

    public void testShowsTitleUsingDefaultHomeStringWhenNoDirectoryIsSpecified() {
        assertEquals(getString(R.string.home), getActivity().getActionBar().getTitle());
    }

    public void testShowsDirectorySpecified() {
        File file = mDirectory.newFile();
        setActivityIntent(newIntent(mDirectory.get()));
        assertEquals(file, getListView().getItemAtPosition(0));
    }

    public void testShowsExternalStorageWhenNoDirectoryIsSpecified() {
        assertEquals(
                getExternalStorageDirectory(),
                ((File) getListView().getItemAtPosition(0)).getParentFile());

        assertEquals(
                getActivity().getString(R.string.home),
                getActivity().getActionBar().getTitle());
    }

    private ListView getListView() {
        return (ListView) getActivity().findViewById(android.R.id.list);
    }

    private Intent newIntent(File directory) {
        return new Intent().putExtra(EXTRA_DIRECTORY, directory.getAbsolutePath());
    }

    private String getString(int resId) {
        return getActivity().getString(resId);
    }
}
