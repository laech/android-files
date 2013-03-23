package com.example.files.app;

import static android.app.ActionBar.DISPLAY_HOME_AS_UP;
import static android.os.Environment.getExternalStorageDirectory;
import static com.example.files.app.FileListActivity.ARG_DIRECTORY;
import static com.example.files.test.Activities.rotate;
import static com.example.files.test.TempDirectory.newTempDirectory;

import java.io.File;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.example.files.R;
import com.example.files.test.TempDirectory;

public final class FileListActivityTest
        extends ActivityInstrumentationTestCase2<FileListActivity> {

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

    public void testShowsCorrectTitleOnRotate() throws Throwable {
        setActivityIntent(newIntent(mDirectory.get()));

        getActivity();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                rotate(getActivity());
            }
        });

        assertEquals(mDirectory.get().getName(), getActivity().getTitle());
    }

    public void testShowsExternalStorageDirIfNoDirectoryIsSpecified() {
        assertEquals(
                getExternalStorageDirectory(),
                ((File) getListView().getItemAtPosition(0)).getParentFile());

        assertEquals(
                getActivity().getString(R.string.home),
                getActivity().getTitle());
    }

    public void testShowsDirectoryNameAsTitle() {
        setActivityIntent(newIntent(mDirectory.get()));
        assertEquals(mDirectory.get().getName(), getActivity().getTitle());
    }

    public void testShowsDirectorySpecified() {
        File file = mDirectory.newFile();
        setActivityIntent(newIntent(mDirectory.get()));
        assertEquals(file, getListView().getItemAtPosition(0));
    }

    public void testShowsTitleOfDirectorySelectedAndDisplayed() throws Throwable {
        final File dir = show(mDirectory.newDirectory());
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertEquals(dir.getName(), getActivity().getTitle());
            }
        });
    }

    public void testEnablesHomeUpButtonOnDisplayOfSubDirectory() throws Throwable {
        show(mDirectory.newDirectory());
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertTrue(0 < (getActivity().getActionBar().getDisplayOptions() & DISPLAY_HOME_AS_UP));
            }
        });
    }

    public void testDisablesHomeUpButtonIfNoDirectoryToGoBackTo() throws Throwable {
        setActivityIntent(newIntent(mDirectory.get()));
        assertEquals(0, (getActivity().getActionBar().getDisplayOptions() & DISPLAY_HOME_AS_UP));
    }

    private File show(final File dir) throws Throwable {
        getActivity();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().show(dir.getAbsolutePath());
            }
        });
        return dir;
    }

    private ListView getListView() {
        return (ListView) getActivity().findViewById(android.R.id.list);
    }

    private Intent newIntent(File directory) {
        return new Intent().putExtra(ARG_DIRECTORY, directory.getAbsolutePath());
    }

}
