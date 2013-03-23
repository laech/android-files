package com.example.files.widget;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;

import junit.framework.TestCase;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.media.ImageMap;
import com.example.files.util.FileSystem;

public final class FileListAdapterTest extends TestCase {

    private TextView mView;
    private File mFile;
    private FileSystem mFileSystem;
    private ImageMap mImageMap;
    private FileListAdapter mAdapter;

    public void testIsEnabledReturnsFalseIfUserHasNoPermissionToReadFile() {
        given(mFileSystem.hasPermissionToRead(mFile)).willReturn(false);
        assertFalse(mAdapter.isEnabled(0));
    }

    public void testViewIsDisabledIfUserHasNoPermissionToReadFile() {
        setAsFileWithReadPermission(mFile, false);
        mAdapter.updateView(mView, mFile);
        verify(mView).setEnabled(false);
    }

    public void testViewIsDisabledIfUserHasNoPermissionToReadDirectory() {
        setAsDirectoryWithReadPermission(mFile, false);
        mAdapter.updateView(mView, mFile);
        verify(mView).setEnabled(false);
    }

    public void testViewShowsFileName() throws Exception {
        given(mFile.getName()).willReturn("a");
        mAdapter.updateView(mView, mFile);
        verify(mView).setText("a");
    }

    public void testViewShowsIcon() {
        given(mImageMap.get(mFile)).willReturn(R.drawable.ic_launcher);
        mAdapter.updateView(mView, mFile);
        verify(mView).setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_launcher, 0, 0, 0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mView = mock(TextView.class);
        mFile = mock(File.class);
        mFileSystem = mock(FileSystem.class);
        mImageMap = mock(ImageMap.class);
        mAdapter = new FileListAdapter(mockContext(), mFileSystem, mImageMap);
        mAdapter.add(mFile);
    }

    private Context mockContext() {
        LayoutInflater inflater = mock(LayoutInflater.class);
        Context ctx = mock(Context.class);
        given(ctx.getSystemService(LAYOUT_INFLATER_SERVICE)).willReturn(inflater);
        return ctx;
    }

    private void setAsFile(File file) {
        given(file.isDirectory()).willReturn(false);
        given(file.isFile()).willReturn(true);
    }

    private void setAsFileWithReadPermission(File file, boolean hasPermission) {
        setAsFile(file);
        given(mFileSystem.hasPermissionToRead(file)).willReturn(hasPermission);
    }

    private void setAsDirectory(File file) {
        given(file.isDirectory()).willReturn(true);
        given(file.isFile()).willReturn(false);
    }

    private void setAsDirectoryWithReadPermission(File file, boolean hasPermission) {
        setAsDirectory(file);
        given(mFileSystem.hasPermissionToRead(file)).willReturn(hasPermission);
    }

}
